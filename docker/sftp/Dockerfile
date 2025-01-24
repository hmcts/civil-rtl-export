FROM atmoz/sftp:alpine

# Change sshd_config so that it sets the sftp user root directory to /home/sftp/amdashboard
# This allows files to be uploaded without having to change to amdashboard directory first.
# I think this is a bit of a hack!
#COPY ./ssh-config/sshd_config /etc/ssh/sshd_config

# Upload host keys.  Ensures the same keys are used each time the container is started.
# Without this the container will generate new keys each time it starts.
# Using the same keys prevents warnings about thumbprint having changed when connecting via sftp.
#COPY ./keys/ssh_host_rsa_key /etc/ssh/ssh_host_rsa_key
#COPY ./keys/ssh_host_ed25519_key /etc/ssh/ssh_host_ed25519_key

# Upload sftp user public key to allow user to log on using SSH.
#COPY ./keys/sftp-docker.pub /home/sftp/.ssh/keys/sftp-docker.pub