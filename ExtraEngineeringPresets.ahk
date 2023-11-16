#Requires AutoHotkey v2.0
#SingleInstance
#Warn

SendMode "Input"

; ----------------------------------------------------------------
; Startup Stuff
; ----------------------------------------------------------------

Run A_ComSpec " /c java -jar ian.jar",,,&ianPid

isEasternFront := IniRead("config.ini", "Misc", "isEasternFront", "0")
presetFilePath := IniRead("config.ini", "Presets", "presetFile", "presets.txt")

; EF uses different reddish colors
if (isEasternFront) {
	BULB_RED_CUTOFF := -1
BULB_GREEN_CUTOFF := -1
	BULB_BLUE_CUTOFF := 64
} else {
	BULB_RED_CUTOFF := 64
	BULB_GREEN_CUTOFF := 161
	BULB_BLUE_CUTOFF := -1
}

; load from file what keybinds are being used for presets
try
	presetFileLines := StrSplit(FileRead(presetFilePath), "`n")
catch OSError as err
	; console window will ask user if they want to generate
	; and edit the file; AHK doesn't need to do anything.
	ExitApp
#HotIf isOkToInterceptKeypress()
HotIf 'isOkToInterceptKeypress()'
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
		Hotkey line, (hotkeyName) => applyPreset(hotkeyName)
		i := i + 8
	}
	i := i + 1
}
#HotIf

; ----------------------------------------------------------------
; Applies a preset by typing its key into IAN's command line
; ----------------------------------------------------------------
applyPreset(key) {
	ControlSendText(key "`n", , "ahk_pid " ianPid)
}

; ----------------------------------------------------------------
; If there is no IAN console window, returns false.
; If an engineering artemis window isn't focused, returns false.
; Otherwise, returns true.
; ----------------------------------------------------------------
isOkToInterceptKeypress() {
	if (!WinExist("ahk_pid " ianPid)) {
		ExitApp
	}
	return checkIfFocusedOnEngWin() > 0
}

; ----------------------------------------------------------------
; Checks if an engineering artemis window currently has focus.
; Returns: ahk_id if the window is found, otherwise returns -1.
; ----------------------------------------------------------------
checkIfFocusedOnEngWin() {
	artemisWinAhkId := WinActive("Game Window")
	if (artemisWinAhkId <= 0) {
		return -1
	}
	
	; This will fail if, for some reason, the engineering console tab
	; is off-screen enough such that the first E is not visible.
	; I don't know how this could happen though, or if it even can:
	; Engineering must be one of the first 4 consoles, and the smallest
	; window size offered by Artemis (800x600) can just barely fit the
	; tabs for 4 consoles.
	
	selectedConsolePos := findSelectedConsole1thru4(artemisWinAhkId)
	;MsgBox("selectedConsolePos=" selectedConsolePos)
	if (selectedConsolePos < 0) {
		return -1
	}
	
	if (isConsoleEng(artemisWinAhkId, selectedConsolePos)) {
		return artemisWinAhkId
	} else {
		return -1
	}
	
	; Alternative: just check for a big 1024 x 768 (or bigger) window
	/*
	WinGetPos &X, &Y, &W, &H, "ahk_id " activeWinId
	if (W >= 1024 && H >= 768) {
		return activeWinId
	} else {
		return -1
	}
	*/
}

; ----------------------------------------------------------------
; Gets which of the first 4 console tabs are selected
; (kind of performance intensive: calls PixelGetColor up to 4 times)
; Params:
;   winAhkId: ahk_id of the window
; Returns: 0 through 3 to indicate which console position is selected
;          if none of the first 4 consoles are selected, returns -1
; ----------------------------------------------------------------
findSelectedConsole1thru4(winAhkId) {
	Loop 4 {
		if (isBulbOn(A_Index - 1)) {
			return A_Index - 1
		}
	}
	return -1
}

isBulbOn(consolePos) {
	colorHex := PixelGetColor(getConsoleBulbX(consolePos), 13)
	; stolen from https://www.autohotkey.com/board/topic/38968-hex-to-rgb/
	red := colorHex >> 16 & 0xFF
	green := colorHex >> 8 & 0xFF
	blue := colorHex & 0xFF
	
	;MsgBox("Bulb " consolePos " (" getConsoleBulbX(consolePos) "," 13 "): R " red " G " green)
	
	if (red < BULB_RED_CUTOFF) {
		return False
	} else if (green < BULB_GREEN_CUTOFF) {
		return False
	} else if (blue < BULB_BLUE_CUTOFF) {
		return False
	} else {
		return True
	}
}

getConsoleBulbX(consolePos) {
	return 129 + (consolePos * 170)
}

; ----------------------------------------------------------------
; Checks if a specific console tab is engineering
; (more performance intensive: calls PixelGetColor up to 10 times)
; Params:
;   winAhkId: ahk_id of the window
;   consolePos: integer for which console to check (starts at 0)
;               if it's 4 or above, isConsoleEng returns 0 b/c
;               engineering must be in one of the first 4 tabs
; Returns: 0 if the console tab is engineering, else 1
; ----------------------------------------------------------------
isConsoleEng(winAhkId, consolePos) {
	if (!(0 <= consolePos && consolePos <= 3)) {
		return 0
	}
	
	; check for an E-shaped thing at the right position
	
	bulbX := getConsoleBulbX(consolePos)
	if (!isPixelWhiteish(bulbX + 57, 17)) {
		;MsgBox("Eng check failed at pixel bulbX+57 17")
		return False
	} else if (!isPixelWhiteish(bulbX + 62, 17)) {
		;MsgBox("Eng check failed at pixel bulbX+62 17")
		return False
	} else if (!isPixelWhiteish(bulbX + 57, 14)) {
		;MsgBox("Eng check failed at pixel bulbX+57 14")
		return False
	} else if (!isPixelBlackish(bulbX + 62, 14)) {
		;MsgBox("Eng check failed at pixel bulbX+62 14")
		return False
	} else if (!isPixelWhiteish(bulbX + 59, 11)) {
		;MsgBox("Eng check failed at pixel bulbX+59 11")
		return False
	} else if (!isPixelWhiteish(bulbX + 62, 11)) {
		;MsgBox("Eng check failed at pixel bulbX+62 11")
		return False
	} else if (!isPixelWhiteish(bulbX + 59, 8)) {
		;MsgBox("Eng check failed at pixel bulbX+59 8")
		return False
	} else if (!isPixelBlackish(bulbX + 64, 8)) {
		;MsgBox("Eng check failed at pixel bulbX+64 8")
		return False
	} else if (!isPixelWhiteish(bulbX + 60, 5)) {
		;MsgBox("Eng check failed at pixel bulbX+60 5")
		return False
	} else if (!isPixelWhiteish(bulbX + 64, 5)) {
		;MsgBox("Eng check failed at pixel bulbX+64 5")
		return False
	} else {
		return True
	}
}

isPixelWhiteish(x, y) {
	colorHex := PixelGetColor(x, y)
	; stolen from https://www.autohotkey.com/board/topic/38968-hex-to-rgb/
	red := colorHex >> 16 & 0xFF
	green := colorHex >> 8 & 0xFF
	blue := colorHex & 0xFF
	
	;MsgBox("pixel at (" x "," y "): " red " " green " " blue)
	
	if (red < 100) {
		return False
	} else if (green < 115) {
		return False
	} else if (blue < 127) {
		return False
	} else {
		return True
	}
}

isPixelBlackish(x, y) {
	colorHex := PixelGetColor(x, y)
	; stolen from https://www.autohotkey.com/board/topic/38968-hex-to-rgb/
	red := colorHex >> 16 & 0xFF
	green := colorHex >> 8 & 0xFF
	blue := colorHex & 0xFF
	
	;MsgBox("pixel at (" x "," y "): " red " " green " " blue)
	
	if (red > 100) {
		return False
	} else if (green > 115) {
		return False
	} else if (blue > 127) {
		return False
	} else {
		return True
	}
}

^Esc::ExitApp
