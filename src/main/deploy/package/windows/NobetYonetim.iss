;This file will be executed next to the application bundle image
;I.e. current directory will contain folder NobetYonetim with application files
[Setup]
AppId={{org.utkuozdemir.watchdist.app}}
AppName=NobetYonetim
AppVersion=1.0
AppVerName=NobetYonetim 1.0
AppPublisher=Nobet
AppComments=NobetYonetim
AppCopyright=Copyright (C) 2015
;AppPublisherURL=http://java.com/
;AppSupportURL=http://java.com/
;AppUpdatesURL=http://java.com/
DefaultDirName={pf}\NobetYonetim
DisableStartupPrompt=Yes
DisableDirPage=No
DisableProgramGroupPage=Yes
DisableReadyPage=Yes
DisableFinishedPage=Yes
DisableWelcomePage=Yes
DefaultGroupName=Nobet
;Optional License
LicenseFile=
;WinXP or above
MinVersion=0,5.1 
OutputBaseFilename=NobetYonetim-1.0
Compression=lzma2/max
SolidCompression=yes
PrivilegesRequired=lowest
SetupIconFile=NobetYonetim\NobetYonetim.ico
UninstallDisplayIcon={app}\NobetYonetim.ico
UninstallDisplayName=NobetYonetim
WizardImageStretch=Yes
WizardSmallImageFile=NobetYonetim-setup-icon.bmp   
ArchitecturesInstallIn64BitMode=

[Tasks]
Name: "desktopicon"; Description: "{cm:CreateDesktopIcon}"; GroupDescription: "{cm:AdditionalIcons}"; Flags: unchecked
Name: desktopicon\common; Description: "For all users"; GroupDescription: "Additional icons:"; Components: main; Flags: exclusive
Name: desktopicon\user; Description: "For the current user only"; GroupDescription: "Additional icons:"; Components: main; Flags: exclusive unchecked

[Languages]
Name: "english"; MessagesFile: "compiler:Default.isl"
Name: "turkish"; MessagesFile: "compiler:Turkish.isl"

[Files]
Source: "NobetYonetim\NobetYonetim.exe"; DestDir: "{app}"; Flags: ignoreversion
Source: "NobetYonetim\*"; DestDir: "{app}"; Flags: ignoreversion recursesubdirs createallsubdirs

[Icons]
Name: "{group}\NobetYonetim"; Filename: "{app}\NobetYonetim.exe"; IconFilename: "{app}\NobetYonetim.ico"; Check: returnTrue()
Name: "{commondesktop}\NobetYonetim"; Filename: "{app}\NobetYonetim.exe";  IconFilename: "{app}\NobetYonetim.ico"; Check: returnFalse()

[Run]
Filename: "{app}\NobetYonetim.exe"; Description: "{cm:LaunchProgram,NobetYonetim}"; Flags: nowait postinstall skipifsilent; Check: returnTrue()
Filename: "{app}\NobetYonetim.exe"; Parameters: "-install -svcName ""NobetYonetim"" -svcDesc ""NobetYonetim"" -mainExe ""NobetYonetim.exe""  "; Check: returnFalse()

;[InstallDelete]
;Type: filesandordirs; Name: {app}\app

[UninstallRun]
Filename: "{app}\NobetYonetim.exe "; Parameters: "-uninstall -svcName NobetYonetim -stopOnUninstall"; Check: returnFalse()

;[UninstallDelete]
;Type: filesandordirs; Name: "{app}\app"

[Code]
function returnTrue(): Boolean;
begin
  Result := True;
end;

function returnFalse(): Boolean;
begin
  Result := False;
end;

function InitializeSetup(): Boolean;
begin
// Possible future improvements:
//   if version less or same => just launch app
//   if upgrade => check if same app is running and wait for it to exit
//   Add pack200/unpack200 support? 
  Result := True;
end;  
