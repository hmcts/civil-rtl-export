The key files in this directory are only used for testing.

The keys are used to prevent the civil-rtl-export-sftp SFTP server running in Docker from generating new keys each time
it is started.  This prevents a warning about the thumbprint being changed when connecting it server via sftp.
