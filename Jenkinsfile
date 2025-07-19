pipeline {
    agent any

    environment {
        ANSIBLE_HOME = '/home/ansible/ansible'
        REMOTE_ARTIFACT_DIR = '/home/ansible/ansible/tmp/jenkins-artifacts'
        WAR_SOURCE_PATH = '/var/lib/jenkins/workspace/Package-job/target/XYZtechnologies-1.0.war' // Update this path
    }
    
    stages {
        stage('Compile and Package') {
            steps {
                build job: 'CompileXYZ_technologies', wait: true, propagate: true
                build job: 'Package-job', wait: true, propagate: true
            }
        }
        
        stage('Verify Files') {
            steps {
                script {
                    // Basic file check that works in all Jenkins environments
                    def exists = fileExists "${env.WAR_SOURCE_PATH}"
                    if (!exists) {
                        sh "ls -la /var/lib/jenkins/workspace/Package-job/target/" // Debug output
                        error "WAR file not found at ${env.WAR_SOURCE_PATH}"
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
                        // 1. Create remote directory
                        sh """
                            ssh -o StrictHostKeyChecking=no -i '$SSH_KEY' root@10.10.10.229 \
                                'mkdir -p ${REMOTE_ARTIFACT_DIR}'
                        """
                        
                        // 2. Simple SCP transfer
                        sh """
                            scp -i '$SSH_KEY' ${env.WAR_SOURCE_PATH} \
                                root@10.10.10.229:${REMOTE_ARTIFACT_DIR}/
                        """
                        
                        // 3. Verify transfer
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
            script {
                echo "Build completed with status: ${currentBuild.result}"
            }
        }
    }
}