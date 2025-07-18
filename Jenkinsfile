pipeline {
    agent any
    
    stages {
        stage('Compile') {
            steps {
                build job: 'CompileXYZ_technologies', wait: true
            }
        }
    post {
        always {
            cleanWs() // Clean workspace after build
        }
    }
}
