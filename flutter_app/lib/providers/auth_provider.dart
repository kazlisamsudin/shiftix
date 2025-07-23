import 'dart:convert';
import 'package:flutter/foundation.dart';
import 'package:http/http.dart' as http;
import 'package:shared_preferences/shared_preferences.dart';
import '../models/user.dart';
import '../services/api_service.dart';

class AuthProvider with ChangeNotifier {
  User? _user;
  bool _isAuthenticated = false;
  bool _isLoading = false;
  String? _errorMessage;

  User? get user => _user;
  bool get isAuthenticated => _isAuthenticated;
  bool get isLoading => _isLoading;
  String? get errorMessage => _errorMessage;

  final ApiService _apiService = ApiService();

  AuthProvider() {
    _loadUserFromStorage();
  }

  Future<void> _loadUserFromStorage() async {
    try {
      final prefs = await SharedPreferences.getInstance();
      final userData = prefs.getString('user_data');
      final token = prefs.getString('auth_token');

      if (userData != null && token != null) {
        _user = User.fromJson(json.decode(userData));
        _isAuthenticated = true;
        _apiService.setAuthToken(token);
        notifyListeners();
      }
    } catch (e) {
      debugPrint('Error loading user from storage: $e');
    }
  }

  Future<bool> login(String username, String password) async {
    _isLoading = true;
    _errorMessage = null;
    notifyListeners();

    try {
      final response = await http.post(
        Uri.parse('${_apiService.baseUrl}/login'),
        headers: {'Content-Type': 'application/x-www-form-urlencoded'},
        body: {
          'username': username,
          'password': password,
        },
      );

      if (response.statusCode == 200) {
        // Extract session cookie or token from response
        final cookies = response.headers['set-cookie'];
        if (cookies != null) {
          final sessionId = _extractSessionId(cookies);
          if (sessionId != null) {
            _apiService.setAuthToken(sessionId);

            // Get user details
            final userResponse = await _apiService.get('/api/user/profile');
            if (userResponse['success']) {
              _user = User.fromJson(userResponse['data']);
              _isAuthenticated = true;

              // Save to storage
              await _saveUserToStorage();

              _isLoading = false;
              notifyListeners();
              return true;
            }
          }
        }
      }

      _errorMessage = 'Invalid username or password';
      _isLoading = false;
      notifyListeners();
      return false;
    } catch (e) {
      _errorMessage = 'Connection error. Please try again.';
      _isLoading = false;
      notifyListeners();
      return false;
    }
  }

  Future<void> logout() async {
    try {
      await _apiService.post('/logout', {});
    } catch (e) {
      debugPrint('Error during logout: $e');
    }

    _user = null;
    _isAuthenticated = false;
    _errorMessage = null;

    // Clear storage
    final prefs = await SharedPreferences.getInstance();
    await prefs.remove('user_data');
    await prefs.remove('auth_token');

    notifyListeners();
  }

  Future<void> _saveUserToStorage() async {
    try {
      final prefs = await SharedPreferences.getInstance();
      await prefs.setString('user_data', json.encode(_user!.toJson()));
      await prefs.setString('auth_token', _apiService.authToken ?? '');
    } catch (e) {
      debugPrint('Error saving user to storage: $e');
    }
  }

  String? _extractSessionId(String cookies) {
    final sessionCookie = cookies.split(';').firstWhere(
      (cookie) => cookie.trim().startsWith('JSESSIONID='),
      orElse: () => '',
    );

    if (sessionCookie.isNotEmpty) {
      return sessionCookie.split('=')[1];
    }
    return null;
  }

  void clearError() {
    _errorMessage = null;
    notifyListeners();
  }
}
