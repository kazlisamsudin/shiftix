import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:intl/intl.dart';
import '../providers/auth_provider.dart';
import '../providers/attendance_provider.dart';
import '../models/attendance_record.dart';

class DashboardScreen extends StatefulWidget {
  const DashboardScreen({Key? key}) : super(key: key);

  @override
  State<DashboardScreen> createState() => _DashboardScreenState();
}

class _DashboardScreenState extends State<DashboardScreen> {
  final TextEditingController _notesController = TextEditingController();

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      Provider.of<AttendanceProvider>(context, listen: false).loadAttendanceData();
    });
  }

  @override
  void dispose() {
    _notesController.dispose();
    super.dispose();
  }

  Future<void> _performAttendanceAction(String action, String title) async {
    final attendanceProvider = Provider.of<AttendanceProvider>(context, listen: false);

    // First, get and validate location
    final hasLocation = await attendanceProvider.getCurrentLocation();
    if (!hasLocation) {
      _showErrorDialog('Location Error', attendanceProvider.errorMessage ?? 'Unable to get location');
      return;
    }

    final isValidLocation = await attendanceProvider.validateLocation();
    if (!isValidLocation) {
      _showErrorDialog('Location Error', 'You are not within range of any registered location');
      return;
    }

    // Show confirmation dialog with notes option
    final confirmed = await _showAttendanceDialog(title);
    if (!confirmed) return;

    // Perform the action
    bool success = false;
    switch (action) {
      case 'checkin':
        success = await attendanceProvider.checkIn(notes: _notesController.text);
        break;
      case 'checkout':
        success = await attendanceProvider.checkOut(notes: _notesController.text);
        break;
      case 'break-start':
        success = await attendanceProvider.startBreak(notes: _notesController.text);
        break;
      case 'break-end':
        success = await attendanceProvider.endBreak(notes: _notesController.text);
        break;
    }

    if (success) {
      _showSuccessDialog('Success', '$title completed successfully!');
    } else {
      _showErrorDialog('Error', attendanceProvider.errorMessage ?? 'Failed to perform action');
    }

    _notesController.clear();
  }

  Future<bool> _showAttendanceDialog(String title) async {
    return await showDialog<bool>(
      context: context,
      builder: (context) => AlertDialog(
        title: Text(title),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            const Text('Are you sure you want to proceed?'),
            const SizedBox(height: 16),
            TextField(
              controller: _notesController,
              decoration: const InputDecoration(
                labelText: 'Notes (Optional)',
                border: OutlineInputBorder(),
              ),
              maxLines: 3,
            ),
          ],
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.of(context).pop(false),
            child: const Text('Cancel'),
          ),
          ElevatedButton(
            onPressed: () => Navigator.of(context).pop(true),
            child: const Text('Confirm'),
          ),
        ],
      ),
    ) ?? false;
  }

  void _showErrorDialog(String title, String message) {
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: Text(title),
        content: Text(message),
        actions: [
          TextButton(
            onPressed: () => Navigator.of(context).pop(),
            child: const Text('OK'),
          ),
        ],
      ),
    );
  }

  void _showSuccessDialog(String title, String message) {
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: Text(title),
        content: Text(message),
        actions: [
          TextButton(
            onPressed: () => Navigator.of(context).pop(),
            child: const Text('OK'),
          ),
        ],
      ),
    );
  }

  Color _getStatusColor(String status) {
    switch (status.toLowerCase()) {
      case 'checked in':
        return Colors.green;
      case 'checked out':
        return Colors.blue;
      case 'on break':
        return Colors.orange;
      default:
        return Colors.grey;
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Shiftix Mobile'),
        actions: [
          PopupMenuButton<String>(
            onSelected: (value) async {
              if (value == 'logout') {
                await Provider.of<AuthProvider>(context, listen: false).logout();
                if (mounted) {
                  Navigator.of(context).pushReplacementNamed('/login');
                }
              }
            },
            itemBuilder: (context) => [
              const PopupMenuItem(
                value: 'logout',
                child: Row(
                  children: [
                    Icon(Icons.logout),
                    SizedBox(width: 8),
                    Text('Logout'),
                  ],
                ),
              ),
            ],
          ),
        ],
      ),
      body: Consumer2<AuthProvider, AttendanceProvider>(
        builder: (context, authProvider, attendanceProvider, child) {
          if (attendanceProvider.isLoading) {
            return const Center(child: CircularProgressIndicator());
          }

          return RefreshIndicator(
            onRefresh: () => attendanceProvider.loadAttendanceData(),
            child: SingleChildScrollView(
              physics: const AlwaysScrollableScrollPhysics(),
              padding: const EdgeInsets.all(16),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  // Welcome Card
                  Card(
                    child: Padding(
                      padding: const EdgeInsets.all(16),
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text(
                            'Welcome, ${authProvider.user?.fullName ?? 'User'}!',
                            style: Theme.of(context).textTheme.headlineSmall,
                          ),
                          const SizedBox(height: 8),
                          Text(
                            'Current Status',
                            style: Theme.of(context).textTheme.titleMedium,
                          ),
                          const SizedBox(height: 8),
                          Container(
                            padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
                            decoration: BoxDecoration(
                              color: _getStatusColor(attendanceProvider.currentStatus),
                              borderRadius: BorderRadius.circular(20),
                            ),
                            child: Text(
                              attendanceProvider.currentStatus,
                              style: const TextStyle(
                                color: Colors.white,
                                fontWeight: FontWeight.bold,
                              ),
                            ),
                          ),
                        ],
                      ),
                    ),
                  ),
                  const SizedBox(height: 16),

                  // Quick Actions
                  Card(
                    child: Padding(
                      padding: const EdgeInsets.all(16),
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text(
                            'Quick Actions',
                            style: Theme.of(context).textTheme.titleMedium,
                          ),
                          const SizedBox(height: 16),
                          GridView.count(
                            shrinkWrap: true,
                            physics: const NeverScrollableScrollPhysics(),
                            crossAxisCount: 2,
                            childAspectRatio: 1.5,
                            crossAxisSpacing: 12,
                            mainAxisSpacing: 12,
                            children: [
                              _buildActionButton(
                                'Check In',
                                Icons.login,
                                Colors.green,
                                attendanceProvider.canPerformAction('checkin'),
                                () => _performAttendanceAction('checkin', 'Check In'),
                              ),
                              _buildActionButton(
                                'Check Out',
                                Icons.logout,
                                Colors.red,
                                attendanceProvider.canPerformAction('checkout'),
                                () => _performAttendanceAction('checkout', 'Check Out'),
                              ),
                              _buildActionButton(
                                'Start Break',
                                Icons.coffee,
                                Colors.orange,
                                attendanceProvider.canPerformAction('break-start'),
                                () => _performAttendanceAction('break-start', 'Start Break'),
                              ),
                              _buildActionButton(
                                'End Break',
                                Icons.play_arrow,
                                Colors.blue,
                                attendanceProvider.canPerformAction('break-end'),
                                () => _performAttendanceAction('break-end', 'End Break'),
                              ),
                            ],
                          ),
                        ],
                      ),
                    ),
                  ),
                  const SizedBox(height: 16),

                  // Recent Attendance
                  Card(
                    child: Padding(
                      padding: const EdgeInsets.all(16),
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text(
                            'Recent Attendance',
                            style: Theme.of(context).textTheme.titleMedium,
                          ),
                          const SizedBox(height: 16),
                          if (attendanceProvider.attendanceHistory.isEmpty)
                            const Center(
                              child: Padding(
                                padding: EdgeInsets.all(32),
                                child: Text(
                                  'No attendance records found',
                                  style: TextStyle(color: Colors.grey),
                                ),
                              ),
                            )
                          else
                            ListView.separated(
                              shrinkWrap: true,
                              physics: const NeverScrollableScrollPhysics(),
                              itemCount: attendanceProvider.attendanceHistory.length > 5
                                  ? 5
                                  : attendanceProvider.attendanceHistory.length,
                              separatorBuilder: (context, index) => const Divider(),
                              itemBuilder: (context, index) {
                                final record = attendanceProvider.attendanceHistory[index];
                                return _buildAttendanceRecord(record);
                              },
                            ),
                        ],
                      ),
                    ),
                  ),
                ],
              ),
            ),
          );
        },
      ),
    );
  }

  Widget _buildActionButton(String title, IconData icon, Color color, bool enabled, VoidCallback onPressed) {
    return ElevatedButton(
      onPressed: enabled ? onPressed : null,
      style: ElevatedButton.styleFrom(
        backgroundColor: enabled ? color : Colors.grey,
        foregroundColor: Colors.white,
        padding: const EdgeInsets.all(12),
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(12),
        ),
      ),
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          Icon(icon, size: 24),
          const SizedBox(height: 4),
          Text(
            title,
            textAlign: TextAlign.center,
            style: const TextStyle(fontSize: 12),
          ),
        ],
      ),
    );
  }

  Widget _buildAttendanceRecord(AttendanceRecord record) {
    return ListTile(
      leading: CircleAvatar(
        backgroundColor: _getTypeColor(record.type),
        child: Icon(
          _getTypeIcon(record.type),
          color: Colors.white,
          size: 20,
        ),
      ),
      title: Text(record.formattedType),
      subtitle: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(record.locationName),
          Text(
            DateFormat('MMM dd, yyyy HH:mm').format(record.timestamp),
            style: const TextStyle(fontSize: 12, color: Colors.grey),
          ),
        ],
      ),
      trailing: Icon(
        record.validLocation ? Icons.check_circle : Icons.warning,
        color: record.validLocation ? Colors.green : Colors.orange,
        size: 20,
      ),
    );
  }

  Color _getTypeColor(String type) {
    switch (type) {
      case 'CHECK_IN':
        return Colors.green;
      case 'CHECK_OUT':
        return Colors.red;
      case 'BREAK_START':
        return Colors.orange;
      case 'BREAK_END':
        return Colors.blue;
      default:
        return Colors.grey;
    }
  }

  IconData _getTypeIcon(String type) {
    switch (type) {
      case 'CHECK_IN':
        return Icons.login;
      case 'CHECK_OUT':
        return Icons.logout;
      case 'BREAK_START':
        return Icons.coffee;
      case 'BREAK_END':
        return Icons.play_arrow;
      default:
        return Icons.access_time;
    }
  }
}
