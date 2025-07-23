import 'dart:convert';
import 'package:flutter/foundation.dart';
import 'package:geolocator/geolocator.dart';
import '../models/attendance_record.dart';
import '../models/location.dart';
import '../services/api_service.dart';

class AttendanceProvider with ChangeNotifier {
  List<AttendanceRecord> _attendanceHistory = [];
  List<Location> _availableLocations = [];
  String _currentStatus = 'Not checked in';
  bool _isLoading = false;
  String? _errorMessage;
  Position? _currentPosition;

  List<AttendanceRecord> get attendanceHistory => _attendanceHistory;
  List<Location> get availableLocations => _availableLocations;
  String get currentStatus => _currentStatus;
  bool get isLoading => _isLoading;
  String? get errorMessage => _errorMessage;
  Position? get currentPosition => _currentPosition;

  final ApiService _apiService = ApiService();

  Future<void> loadAttendanceData() async {
    _isLoading = true;
    notifyListeners();

    try {
      // Load current status
      final statusResponse = await _apiService.get('/api/attendance/status');
      if (statusResponse['success']) {
        _currentStatus = statusResponse['data']['status'];
      }

      // Load attendance history
      final historyResponse = await _apiService.get('/api/attendance/history');
      if (historyResponse['success']) {
        _attendanceHistory = (historyResponse['data'] as List)
            .map((json) => AttendanceRecord.fromJson(json))
            .toList();
      }

      // Load available locations
      final locationsResponse = await _apiService.get('/api/attendance/locations');
      if (locationsResponse['success']) {
        _availableLocations = (locationsResponse['data'] as List)
            .map((json) => Location.fromJson(json))
            .toList();
      }

      _errorMessage = null;
    } catch (e) {
      _errorMessage = 'Failed to load attendance data';
      debugPrint('Error loading attendance data: $e');
    }

    _isLoading = false;
    notifyListeners();
  }

  Future<bool> getCurrentLocation() async {
    try {
      // Check location permissions
      LocationPermission permission = await Geolocator.checkPermission();
      if (permission == LocationPermission.denied) {
        permission = await Geolocator.requestPermission();
        if (permission == LocationPermission.denied) {
          _errorMessage = 'Location permissions are denied';
          notifyListeners();
          return false;
        }
      }

      if (permission == LocationPermission.deniedForever) {
        _errorMessage = 'Location permissions are permanently denied';
        notifyListeners();
        return false;
      }

      // Get current position
      _currentPosition = await Geolocator.getCurrentPosition(
        desiredAccuracy: LocationAccuracy.high,
      );

      return true;
    } catch (e) {
      _errorMessage = 'Failed to get current location';
      debugPrint('Error getting location: $e');
      notifyListeners();
      return false;
    }
  }

  Future<bool> validateLocation() async {
    if (_currentPosition == null) {
      if (!await getCurrentLocation()) {
        return false;
      }
    }

    try {
      final response = await _apiService.post('/api/attendance/validate-location', {
        'latitude': _currentPosition!.latitude,
        'longitude': _currentPosition!.longitude,
      });

      if (response['success']) {
        return response['data']['valid'];
      }
      return false;
    } catch (e) {
      _errorMessage = 'Failed to validate location';
      debugPrint('Error validating location: $e');
      notifyListeners();
      return false;
    }
  }

  Future<bool> performAttendanceAction(String action, {String? notes}) async {
    if (_currentPosition == null) {
      if (!await getCurrentLocation()) {
        return false;
      }
    }

    _isLoading = true;
    _errorMessage = null;
    notifyListeners();

    try {
      final requestData = {
        'latitude': _currentPosition!.latitude,
        'longitude': _currentPosition!.longitude,
        'notes': notes ?? '',
      };

      final response = await _apiService.post('/api/attendance/$action', requestData);

      if (response['success']) {
        // Reload attendance data to get updated status
        await loadAttendanceData();
        _isLoading = false;
        notifyListeners();
        return true;
      } else {
        _errorMessage = response['message'] ?? 'Failed to perform attendance action';
      }
    } catch (e) {
      _errorMessage = 'Connection error. Please try again.';
      debugPrint('Error performing attendance action: $e');
    }

    _isLoading = false;
    notifyListeners();
    return false;
  }

  Future<bool> checkIn({String? notes}) async {
    return await performAttendanceAction('checkin', notes: notes);
  }

  Future<bool> checkOut({String? notes}) async {
    return await performAttendanceAction('checkout', notes: notes);
  }

  Future<bool> startBreak({String? notes}) async {
    return await performAttendanceAction('break/start', notes: notes);
  }

  Future<bool> endBreak({String? notes}) async {
    return await performAttendanceAction('break/end', notes: notes);
  }

  void clearError() {
    _errorMessage = null;
    notifyListeners();
  }

  bool canPerformAction(String action) {
    switch (action) {
      case 'checkin':
        return _currentStatus == 'Not checked in' || _currentStatus == 'Checked out';
      case 'checkout':
        return _currentStatus == 'Checked in';
      case 'break-start':
        return _currentStatus == 'Checked in';
      case 'break-end':
        return _currentStatus == 'On break';
      default:
        return false;
    }
  }
}
