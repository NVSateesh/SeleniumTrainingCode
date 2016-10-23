Class QTP
	Dim oFSO, oFile
	
	Sub Class_Initialize()
		'to enable local logging for debugging
		'Set oFSO =  CreateObject("Scripting.filesystemobject")
		'Set oFile = oFSO.OpenTextFile("D:\\TestTable\\qtpcommands.log", 2,true)
	End Sub
	
   Function Run(ByVal sCommand)
		Dim returnVal
		returnVal = Empty
		poErrorMessage = ""
			On Error Resume Next
					 returnVal = Eval(sCommand)
					 If Not IsEmpty(returnVal) And Err.Number = 0 And poErrorMessage = "" Then
						  Run= returnVal & "||" 
					 ElseIf Err.Number <> 0  Or returnVal = False  Or poErrorMessage <> "" Then
   						If Trim(poErrorMessage)="" And Err.Description <> "" Then
							poErrorMessage = Err.Description 
						ElseIf Trim(poErrorMessage)="" And Err.Description = ""  And Err.Number <>0 Then
							poErrorMessage = Err.Number & " : no error description returned " 
						End If
						Run= returnVal & "||" & poErrorMessage
						poErrorMessage =""
					Else
					   Run= returnVal & "||" 
					End If
			On Error GoTo 0
   End Function

	Function log(sData)
		Set oFSO =  CreateObject("Scripting.filesystemobject")
		Set oFile = oFSO.OpenTextFile(TestArgs("logFile"), 8,true)
		oFile.WriteLine Date & " " & Time & "  [qtp] " & sData
		oFile.Close
		Set oFSO =Nothing
	End Function

	Function CaptureImage(ByVal sFilePath)
	   Desktop.CaptureBitmap sFilePath
	End Function

   Function Evaluate(sCommand)
		Evaluate = Eval(sCommand)
   End Function

	Function SetElementWaitTime(ByVal nTime)
			Set oCurrQTPInstance = GetObject(Empty,"Quicktest.application")
			oCurrQTPInstance.Test.Settings.Run.ObjectSyncTimeOut=nTime
			Set oCurrQTPInstance = Nothing
	End Function

	Function SetPageTimeOut(ByVal nTime)
			Set oCurrQTPInstance = GetObject(Empty,"Quicktest.application")
			oCurrQTPInstance.Test.Settings.Web.BrowserNavigationTimeout = nTime
			Set oCurrQTPInstance = Nothing
	End Function
	
End Class

Class ProxyQTPObject
	Dim oQTPObject

	Sub setObject(ByVal oQTPObject)
		Set oQTPObject = oQTPObject
	End Sub
   
	Function GetProperty(ByVal sProperty)
		GetROProperty = oQTPObject.getROProperty(sProperty)
	End Function

	Function GetCellData(ByVal nRow, ByVal nCol)
		GetCellData = oQTPObject.GetCellData(nRow, nCol)
	End Function

End Class

'MsgBox "in QTP"
Set oExec = New QTP
Set oQTPProxyObject = New ProxyQTPObject

Set xAFTPlugin = CreateObject("com.ags.aft.engine.plugin.QTP")
xAFTPlugin.Runtime oExec
xAFTPlugin.setProxyToolObject oQTPProxyObject
xAFTPlugin.privateKey = TestArgs("inpkey")
TestArgs("inpkey")=""
xAFTPlugin.StartListener(TestArgs("port"))
CloseBrowsers

Sub CloseBrowsers
	Dim oDesc, x
	Set oDesc = Description.Create
	oDesc( "micclass" ).Value = "Browser"
	For x = Desktop.ChildObjects(oDesc).Count - 1 To 0 Step -1
		If Browser( "creationtime:=" & 0 ).Exist(1) Then  Browser( "creationtime:=" & 0 ).Close
	Next
End Sub