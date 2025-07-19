pipeline {
    agent any

    environment {
        ANSIBLE_HOME = '/home/ansible/ansible'
        REMOTE_ARTIFACT_DIR = '/home/ansible/ansible/tmp/jenkins-artifacts'
    }
    
    stages {
        stage('Compile and Package') {
            steps {
                build job: 'CompileXYZ_technologies', wait: true, propagate: true
                build job: 'Package-job', wait: true, propagate: true
            }
        }
        
        stage('Verify Local WAR File') {
            steps {
                script {
                    // Verify the .war file exists before transfer
                    def warFiles = findFiles(glob: '**/target/*.war')
                    if (warFiles.length == 0) {
                        error "No WAR files found in workspace! Check Package-job artifacts."
                    }
                    echo "Found WAR file: ${warFiles[0].path}"
                    env.WAR_FILE_PATH = warFiles[0].path
                }
            }
        }
        
        stage('Test SSH Connection') {
            steps {
                script {
                    withCredentials([sshUserPrivateKey(
                        credentialsId: 'Ans2-ssh-key',
                        keyFileVariable: 'SSH_KEY'
                    )]) {
                        sh """
                            ssh -o StrictHostKeyChecking=no -i '$SSH_KEY' root@10.10.10.229 \
                                'mkdir -p ${REMOTE_ARTIFACT_DIR} && ls -la ${REMOTE_ARTIFACT_DIR}'
                        """
                    }
                }
            }
        }
        
        stage('Transfer WAR File') {
            steps {
                script {
                    withCredentials([sshUserPrivateKey(
                        credentialsId: 'Ans2-ssh-key',
                        keyFileVariable: 'SSH_KEY'
                    )]) {
                        // First clean remote directory
                        sh """
                            ssh -i '$SSH_KEY' root@10.10.10.229 \
                                'rm -f ${REMOTE_ARTIFACT_DIR}/*.war'
                        """
                        
                        // Manual SCP transfer with verbose output
                        sh """
                            scp -v -i '$SSH_KEY' ${env.WAR_FILE_PATH} \
                                root@10.10.10.229:${REMOTE_ARTIFACT_DIR}/
                        """
                        
                        // Verify transfer
                        sh """
                            ssh -i '$SSH_KEY' root@10.10.10.229 \
                                'ls -la ${REMOTE_ARTIFACT_DIR} && chmod 644 ${REMOTE_ARTIFACT_DIR}/*.war'
                        """
                    }
                }
            }
        }
    }
    
    post {
        always {
            cleanWs()
        }
    }
}