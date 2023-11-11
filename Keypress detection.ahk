#Requires AutoHotkey v2.0
#SingleInstance
#Warn

SendMode "Input"

; load from file what keybinds are being used for presets

presetFilePath := IniRead("config.ini", "Presets", "presetFile", "presets.txt")
presetFileLines := StrSplit(FileRead(presetFilePath), "`n")

i := 1
while (i < presetFileLines.Length) {
	line := presetFileLines[i]
	commentPos := InStr(line, ";")
	if (commentPos > 0) {
		line := SubStr(line, 1, commentPos - 1) ; strip comment
	}
	line := StrReplace(line, "`r") ; remove CR
	line := StrReplace(line, "`n") ; remove LF
	line := Trim(line) ; trim spaces and tabs
	if (StrLen(line) > 0) {
		; dynamically set hotkey for each key from the file
		Hotkey line, (hotkeyName) => tryApplyPreset(hotkeyName)
		i := i + 8
	}
	i := i + 1
}

; ----------------------------------------------------------------
; If an engineering artemis window isn't focused, does nothing.
; If there is no IAN console window, does nothing.
; Otherwise, sends key to IAN console (w/o moving focus to it).
; ----------------------------------------------------------------
tryApplyPreset(key)
{
	winTitle := "ian ahk_class ConsoleWindowClass"
	if (checkIfFocusedOnEngWin() > 0 && WinExist(winTitle))
	{
		ControlSendText(key, , winTitle)
		ControlSend("{Enter}", , winTitle)
	}
}

; ----------------------------------------------------------------
; Checks if an engineering artemis window currently has focus.
; If it's found, returns its ahk_id, otherwise returns -1.
; ----------------------------------------------------------------
checkIfFocusedOnEngWin()
{
	activeWinId := WinActive("Game Window")
	if (activeWinId <= 0)
	{
		return -1
	}
	
	; For now, just check for a big 1024 x 768 (or bigger) window
	; TODO check if the window has the engineering station selected
	
	WinGetPos &X, &Y, &W, &H, "ahk_id " activeWinId
	if (W >= 1024 && H >= 768)
	{
		return activeWinId
	}
	else
	{
		return -1
	}
}

^Esc::ExitApp
