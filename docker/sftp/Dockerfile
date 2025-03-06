FROM atmoz/sftp:alpine

COPY ./keys/ssh_host_rsa_key /etc/ssh/ssh_host_rsa_key
COPY ./keys/ssh_host_ed25519_key /etc/ssh/ssh_host_ed25519_key

HEALTHCHECK --interval=10s --timeout=10s --retries=10 CMD netstat -an | grep "0.0.0.0:22" -q
