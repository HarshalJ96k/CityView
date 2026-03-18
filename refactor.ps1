$baseDir = "app\src\main\java\com\example\cityview"

$map = @{
  "CitizenDashboardActivity" = "activities"; "ContactSupportActivity" = "activities"; "EditProfileActivity" = "activities"; "LoginActivity" = "activities"; "NotificationsActivity" = "activities"; "OfficialDashboardActivity" = "activities"; "ProfileActivity" = "activities"; "RegistrationActivity" = "activities"; "SplashActivity" = "activities"; "UpdateAdminProfileActivity" = "activities"
  "ChatAdapter" = "adapters"; "HighlightAdapter" = "adapters"; "NotificationAdapter" = "adapters"; "ReportAdapter" = "adapters"; "UserAdapter" = "adapters"
  "Highlight" = "models"; "Message" = "models"; "NotificationModel" = "models"; "Report" = "models"; "User" = "models"
  "SessionManager" = "utils"; "VolleyMultipartRequest" = "utils"
}

@("activities", "adapters", "models", "utils") | ForEach-Object {
    New-Item -ItemType Directory -Force -Path "$baseDir\$_" | Out-Null
}

$keys = @($map.Keys)
foreach ($cls in $keys) {
    $pkg = $map[$cls]
    $file = "$baseDir\$cls.java"
    if (Test-Path $file) {
        $text = Get-Content $file -Raw
        $text = $text -replace "package com\.example\.cityview;", "package com.example.cityview.$pkg;"
        Set-Content -Path $file -Value $text
        Move-Item $file "$baseDir\$pkg\" -Force
    }
}

$allJavaFiles = Get-ChildItem -Path $baseDir -Recurse -Filter "*.java"
foreach ($f in $allJavaFiles) {
    if ($f.FullName -match "\\urls\\") { continue }
    $text = Get-Content $f.FullName -Raw
    
    $wildcards = "`r`nimport com.example.cityview.activities.*;`r`nimport com.example.cityview.adapters.*;`r`nimport com.example.cityview.models.*;`r`nimport com.example.cityview.utils.*;"
    
    if ($text -notmatch "import com\.example\.cityview\.activities\.\*;") {
        $text = $text -replace "(package com\.example\.cityview(\.[a-zA-Z0-9]+)*;)", "`$1$wildcards"
    }

    foreach ($cls in $keys) {
        $pkg = $map[$cls]
        
        # fix explicit imports
        $text = $text -replace "import com\.example\.cityview\.$cls;", "import com.example.cityview.$pkg.$cls;"
        
    }
    
    if ($text -notmatch "import com\.example\.cityview\.R;") {
        $text = $text -replace "(package com\.example\.cityview(\.[a-zA-Z0-9]+)*;)", "`$1`r`nimport com.example.cityview.R;"
    }

    Set-Content -Path $f.FullName -Value $text
}

$manifest = "app\src\main\AndroidManifest.xml"
$mcontent = Get-Content $manifest -Raw
foreach ($cls in $keys) {
    if ($map[$cls] -eq "activities") {
        $mcontent = $mcontent -replace "android:name=""\.$cls""", "android:name="".activities.$cls"""
        $mcontent = $mcontent -replace "android:name=""com\.example\.cityview\.$cls""", "android:name=""com.example.cityview.activities.$cls"""
    }
}
Set-Content -Path $manifest -Value $mcontent

$layouts = Get-ChildItem -Path "app\src\main\res\layout" -Filter "*.xml"
foreach ($l in $layouts) {
    $lcontent = Get-Content $l.FullName -Raw
    foreach ($cls in $keys) {
        $pkg = $map[$cls]
        $lcontent = $lcontent -replace "tools:context=""\.$cls""", "tools:context="".activities.$cls"""
        $lcontent = $lcontent -replace "tools:context=""com\.example\.cityview\.$cls""", "tools:context=""com.example.cityview.$pkg.$cls"""
        $lcontent = $lcontent -replace "android:name=""com\.example\.cityview\.$cls""", "android:name=""com.example.cityview.$pkg.$cls"""
    }
    Set-Content -Path $l.FullName -Value $lcontent
}

Write-Output "Refactoring complete."
