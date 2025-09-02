Set WshShell = CreateObject("WScript.Shell")

' Path to your Java executable (adjust if needed)
javaPath = "java"

' Path to your JAR file (adjust to the actual location)
jarPath = "D:\Code\BalanceApp\out\artifacts\BalanceApp_jar\BalanceApp.jar"

' Build command line
cmd = javaPath & " -jar """ & jarPath & """"

' Run the command (0 = hide window, True = wait for completion)
WshShell.Run cmd, 0, False