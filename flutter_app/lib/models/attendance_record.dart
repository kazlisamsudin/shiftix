class AttendanceRecord {
  final int id;
  final String userName;
  final String locationName;
  final String type;
  final DateTime timestamp;
  final double latitude;
  final double longitude;
  final double distanceFromLocation;
  final String? notes;
  final bool validLocation;

  AttendanceRecord({
    required this.id,
    required this.userName,
    required this.locationName,
    required this.type,
    required this.timestamp,
    required this.latitude,
    required this.longitude,
    required this.distanceFromLocation,
    this.notes,
    required this.validLocation,
  });

  factory AttendanceRecord.fromJson(Map<String, dynamic> json) {
    return AttendanceRecord(
      id: json['id'],
      userName: json['userName'],
      locationName: json['locationName'],
      type: json['type'],
      timestamp: DateTime.parse(json['timestamp']),
      latitude: json['latitude'].toDouble(),
      longitude: json['longitude'].toDouble(),
      distanceFromLocation: json['distanceFromLocation'].toDouble(),
      notes: json['notes'],
      validLocation: json['validLocation'],
    );
  }

  String get formattedType {
    return type.replaceAll('_', ' ').toLowerCase().split(' ').map((word) =>
        word[0].toUpperCase() + word.substring(1)).join(' ');
  }
}

class Location {
  final int id;
  final String name;
  final String address;
  final double latitude;
  final double longitude;
  final double radiusInMeters;
  final String? description;

  Location({
    required this.id,
    required this.name,
    required this.address,
    required this.latitude,
    required this.longitude,
    required this.radiusInMeters,
    this.description,
  });

  factory Location.fromJson(Map<String, dynamic> json) {
    return Location(
      id: json['id'],
      name: json['name'],
      address: json['address'],
      latitude: json['latitude'].toDouble(),
      longitude: json['longitude'].toDouble(),
      radiusInMeters: json['radiusInMeters'].toDouble(),
      description: json['description'],
    );
  }
}
