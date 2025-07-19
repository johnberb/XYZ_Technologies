pipeline {
    agent any

    environment {
        ANSIBLE_HOME = '/home/ansible/ansible'
        BUILD_NUMBER = "${env.BUILD_ID}"
        REMOTE_ARTIFACT_DIR = '/home/ansible/ansible/tmp/jenkins-artifacts'
    }
    
    stages {
        stage('Compile and Package') {
            steps {
                script {
                    // Trigger compile job and wait for completion
                    def compileBuild = build job: 'CompileXYZ_technologies', wait: true, propagate: true
                    
                    // Wait for Package-job to complete (using build step with parameters if needed)
                    def packageBuild = build job: 'Package-job', wait: true, propagate: true
                    
                    // Store the Package-job's workspace path
                    env.PACKAGE_WORKSPACE = "/var/lib/jenkins/workspace/Package-job" // Default path
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
                            ssh -o StrictHostKeyChecking=no -i '$SSH_KEY' root@10.10.10.229 'whoami && pwd && mkdir -p ${REMOTE_ARTIFACT_DIR}'
                        """
                    }
                }
            }
        }
        
        stage('Transfer WAR File') {
            steps {
                sshPublisher(
                    publishers: [
                        sshPublisherDesc(
                            configName: 'Ansible',
                            transfers: [
                                sshTransfer(
                                    sourceFiles: "${env.PACKAGE_WORKSPACE}/target/*.war",
                                    removePrefix: "${env.PACKAGE_WORKSPACE}/target",
                                    remoteDirectory: REMOTE_ARTIFACT_DIR,
                                    execCommand: "chmod 644 ${REMOTE_ARTIFACT_DIR}/*.war"
                                )
                            ],
                            verbose: true
                        )
                    ]
                )
            }
        }
    }
    
    post {
        always {
            cleanWs() 
        }
    }
}