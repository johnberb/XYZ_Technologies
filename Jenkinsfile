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
        // STAGE 5: Build Docker Image
        stage('Run Ansible Playbook') {
            steps {
                withCredentials([sshUserPrivateKey(
                    credentialsId: 'Ans2-ssh-key',
                    keyFileVariable: 'SSH_KEY'
                )]) {
                    sh '''
                        # 1. Copy Dockerfile.j2 from Jenkins to Ansible server
                        scp -i "$SSH_KEY" \
                            "/var/lib/jenkins/workspace/BuildingXYZTechnologies/Dockerfile.j2" \
                            ansible@10.10.10.229:"/home/ansible/ansible/files/"

                        # 2. Verify file transfer
                        ssh -i "$SSH_KEY" ansible@10.10.10.229 \
                            "ls -l /home/ansible/ansible/files/Dockerfile.j2"
                    
                        #copy private key temporarily onto the ansible server
                        scp -i "$SSH_KEY" "$SSH_KEY" ansible@10.10.10.229:/home/ansible/.ssh/jenkins_key
                        ssh -i "$SSH_KEY" ansible@10.10.10.229 "chmod 600 ~/.ssh/jenkins_key"
  
                        # Test SSH connection first
                        ssh -i "$SSH_KEY" ansible@10.10.10.229 "echo 'SSH test successful'"
        
                        # Run Ansible PLAYBOOK on the Ansible server 
                        #ssh -i "$SSH_KEY" ansible@10.10.10.229 "
                            # Run the playbook
                            #cd /home/ansible/ansible &&
                            #ansible-playbook \
                                #-i /etc/ansible/hosts \
                                #playbooks/docker_build.yml \
                                #--extra-vars 'artifact_path=/tmp/jenkins-artifacts/ABCtechnologies-1.0.war'
                        #"
                    '''
                }
            }
        }
        
    }
    
    post {
        always {
            script {
                echo "Build completed with status: ${currentBuild.result}"
            }
        }
    }
}