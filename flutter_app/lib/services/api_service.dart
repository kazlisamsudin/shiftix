import 'dart:convert';
import 'package:http/http.dart' as http;

class ApiService {
  static const String baseUrl = 'http://localhost:8080'; // Change for production
  String? _authToken;

  String? get authToken => _authToken;

  void setAuthToken(String token) {
    _authToken = token;
  }

  Map<String, String> get _headers {
    final headers = {
      'Content-Type': 'application/json',
      'Accept': 'application/json',
    };

    if (_authToken != null) {
      headers['Cookie'] = 'JSESSIONID=$_authToken';
    }

    return headers;
  }

  Future<Map<String, dynamic>> get(String endpoint) async {
    try {
      final response = await http.get(
        Uri.parse('$baseUrl$endpoint'),
        headers: _headers,
      );

      return _handleResponse(response);
    } catch (e) {
      throw Exception('Network error: $e');
    }
  }

  Future<Map<String, dynamic>> post(String endpoint, Map<String, dynamic> data) async {
    try {
      final response = await http.post(
        Uri.parse('$baseUrl$endpoint'),
        headers: _headers,
        body: json.encode(data),
      );

      return _handleResponse(response);
    } catch (e) {
      throw Exception('Network error: $e');
    }
  }

  Future<Map<String, dynamic>> put(String endpoint, Map<String, dynamic> data) async {
    try {
      final response = await http.put(
        Uri.parse('$baseUrl$endpoint'),
        headers: _headers,
        body: json.encode(data),
      );

      return _handleResponse(response);
    } catch (e) {
      throw Exception('Network error: $e');
    }
  }

  Future<Map<String, dynamic>> delete(String endpoint) async {
    try {
      final response = await http.delete(
        Uri.parse('$baseUrl$endpoint'),
        headers: _headers,
      );

      return _handleResponse(response);
    } catch (e) {
      throw Exception('Network error: $e');
    }
  }

  Map<String, dynamic> _handleResponse(http.Response response) {
    final Map<String, dynamic> responseData = {
      'success': false,
      'data': null,
      'message': '',
    };

    try {
      if (response.body.isNotEmpty) {
        final jsonData = json.decode(response.body);

        if (response.statusCode >= 200 && response.statusCode < 300) {
          responseData['success'] = true;
          responseData['data'] = jsonData;
        } else {
          responseData['message'] = jsonData['message'] ?? 'Request failed';
        }
      } else {
        if (response.statusCode >= 200 && response.statusCode < 300) {
          responseData['success'] = true;
        } else {
          responseData['message'] = 'Request failed with status: ${response.statusCode}';
        }
      }
    } catch (e) {
      responseData['message'] = 'Failed to parse response';
    }

    return responseData;
  }
}
