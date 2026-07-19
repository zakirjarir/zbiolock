Pod::Spec.new do |s|
  s.name             = 'Zbiolock'
  s.version          = '1.0.0'
  s.summary          = 'Secure biometric authentication & token storage for Capacitor.'
  s.homepage         = 'https://github.com/zakirjarir/zbiolock'
  s.license          = { :type => 'MIT', :file => 'LICENSE' }
  s.author           = { 'Zakir Jarir' => 'zakirjarir@example.com' }
  s.source           = { :git => 'https://github.com/zakirjarir/zbiolock.git', :tag => s.version.to_s }

  s.ios.deployment_target = '15.0'
  s.swift_version         = '5.9'

  s.source_files         = 'ios/Sources/ZBioLockPlugin/**/*.{swift,h,m}'
  s.dependency 'Capacitor'

  # LocalAuthentication and Security are system frameworks — included automatically
  s.frameworks = 'LocalAuthentication', 'Security'
end
