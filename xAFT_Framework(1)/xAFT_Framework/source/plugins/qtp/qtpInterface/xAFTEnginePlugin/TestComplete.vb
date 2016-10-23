Imports System.Runtime.InteropServices
Imports System.Threading
Imports System.IO
Imports System.Net
Imports System.Net.Sockets
Imports System.Configuration
Imports System.Text
Imports System.Security.Cryptography

Public Class TestComplete


    Dim psRootObject As String
    Dim psCurrentPage As String
    Dim _Test As String
    'Public Shared _oExecute ' -This is modified to be used from CommonConfig._ToolProxyObject
    Public Shared _scriptRootPath
    Public Shared _browserType
    Public Shared _key
    Public Shared _port
    Public Shared _HighlightObjects
    Public Shared _DefaultObjectTimeout
    Public Shared _CloseQTPOnCompletion
    Public Shared _ORArray(-1)
    Public Shared _PageLoadTimeout
    Public Shared App 'As Application
    Public Shared _orPath

    Private Const Const_ObjectSyncTimeout_ms = "10000"
    Private Const Const_PageLoadTimeout_ms = "60000"
    Private Const Const_DisableSmartIdentification = "true"
    Private Const Const_ListenerPort = "55556"
    Private Const Const_AddinsList = "web"
    Private Const Const_HideQTPWhileExecution = "no"
    Private Const Const_HighlightObjects = "no"
    Private Const Const_CloseQTPOnCompletion = "no"
    Private Const Const_QTPRunInFastMode = "yes"
    Public Shared oConfigDict As Dictionary(Of String, String)
    Public Shared libraryList As List(Of String)


    Public Sub New()
        'nothing
        oConfigDict = New Dictionary(Of String, String)
        CommonConfig.executionEngine = "testComplete"
        libraryList = New List(Of String)
    End Sub

    Public Sub Runtime(ByVal obj)
        CommonConfig._ToolInstance = obj
    End Sub

    Public Sub setProxyToolObject(ByVal obj)
        CommonConfig._ProxyToolObject = obj
    End Sub


    Public Property Test
        Get
            Test = _Test
        End Get

        Set(ByVal value)
            _Test = value
        End Set
    End Property

    Public Property scriptRootPath
        Get
            scriptRootPath = _scriptRootPath
        End Get

        Set(ByVal value)
            _scriptRootPath = value
        End Set
    End Property

    Public WriteOnly Property privateKey
        Set(ByVal value)
            _key = value
        End Set
    End Property


    Public Sub SyncListner()
        Commands.SyncListner()
    End Sub

    Public Sub StopQTP()
        Dim ret, errMsg
        ret = ""
        errMsg = ""
        If Not Commands.runCommand("stoptest", ret, errMsg) Then
            Throw (New Exception("Unable to stop Test Complete:" & errMsg))
        End If
        If _CloseQTPOnCompletion = "yes" Then
            Do
                Thread.Sleep(1000)
            Loop Until LCase(Trim(App.Test.LastRunResults.Status)) <> "running" Or LCase(Trim(App.Test.LastRunResults.Status)) <> "paused"
            Thread.Sleep(25000)
            App.Quit()
        End If
    End Sub

    Function GetBrowserVersion(ByVal sBrowser)
        Dim oFSO, sBrowserPath
        'Check If this is 64 bit machine / 32 bit
        oFSO = CreateObject("Scripting.FileSystemObject")
        If oFSO.FolderExists("C:\Program Files (x86)") Then
            sBrowserPath = "C:\Program Files (x86)"
        Else
            sBrowserPath = "C:\Program Files"
        End If

        Select Case LCase(Trim(sBrowser))
            Case "ie", "internet explorer", "*ie", "*internet explorer", "*internetexplorer", "iexplore"
                GetBrowserVersion = "Microsoft Internet Explorer " & oFSO.GetFileVersion(sBrowserPath & "\Internet Explorer\iexplore.exe")
            Case "ff", "firefox", "*ff", "*firefox"
                GetBrowserVersion = "Firefox " & oFSO.GetFileVersion(sBrowserPath & "\Mozilla Firefox\firefox.exe")
            Case "chrome", "google chrome", "googlechrome"
                GetBrowserVersion = "Google Chrome"
                'GetBrowserVersion = "Firefox " & oFSO.GetFileVersion(sBrowserPath & "\Mozilla Firefox\firefox.exe")
        End Select
        oFSO = Nothing
    End Function

    Public Sub launchAndRunTestComplete(ByVal sDriverScriptPath, ByVal nPort)
        Dim TestCompleteObject

        TestCompleteObject = Nothing

        TestCompleteObject = CreateObject("TestComplete.TestCompleteApplication")

        TestCompleteObject.Visible = True
        ' Obtains access to TestComplete
        'Try
        'TestCompleteObject = Marshal.GetActiveObject(TCProgID)
        'Catch
        'Try
        'TestCompleteObject = Activator.CreateInstance(Type.GetTypeFromProgID(TCProgID))
        'Catch
        'End Try
        'End Try

        If (TestCompleteObject Is Nothing) Then Exit Sub

        ' Obtains Integration object
        Dim IntegrationObject

        IntegrationObject = TestCompleteObject.Integration()

        ' We have a reference to the integration object.
        ' Now we can use its methods and properties to automate TestComplete.

        ' Loads the project suite
        IntegrationObject.OpenProjectSuite(sDriverScriptPath)

        ' Checks whether the project suite was opened
        ' If the project suite cannot be opened, closes TestComplete
        If (Not IntegrationObject.IsProjectSuiteOpened) Then
            MsgBox("Could not open the project suite.")
            ' Closes TestComplete
            TestCompleteObject.Quit()
            ' Releases COM objects 
            Marshal.ReleaseComObject(IntegrationObject)
            Marshal.ReleaseComObject(TestCompleteObject)
            Exit Sub
        End If

        oConfigDict.Item("logFile") = Trim(Replace(oConfigDict.Item("logFile"), "./", _scriptRootPath & "/"))
        oConfigDict.Item("logFile") = Trim(Replace(oConfigDict.Item("logFile"), "/", "\"))
        Try
            ' Runs the test
            Dim oParameters(1)
            oParameters(0) = nPort
            oParameters(1) = oConfigDict.Item("logFile")

            IntegrationObject.RunRoutineEx("driver", "ObjectMap", "StartTest", oParameters)
        Catch ex As System.Runtime.InteropServices.COMException
            MsgBox("An exception occurred: " + ex.Message)
        Finally
            ' Closes TestComplete
            'TestCompleteObject.Quit()
            ' Releases COM objects 
            'Marshal.ReleaseComObject(IntegrationObject)
            'Marshal.ReleaseComObject(TestCompleteObject)
        End Try
    End Sub

    Public Function ExecuteAnnotation(ByVal annotationName, ByVal annotationValue)
        Dim ret, errmsg
        annotationName = Mid(annotationName, 2)
        Select Case Trim(LCase(annotationName))
            Case "pageloadtimeout_ms"
                If LCase(Trim(annotationValue)) = "default" Then
                    annotationValue = GetNodeFULL(_scriptRootPath, "//TestEnvironment/PageLoadTimeout_ms", 1).GetAttribute("value")  'Set the Browser Navigation Timeout
                    'annotationValue = CDbl(QTPInterface._PageLoadTimeout) / 1000
                    annotationValue = CDbl(annotationValue) / 1000
                End If
                Commands.runCommand("SetPageTimeOut(" + CStr(annotationValue) + ")", ret, errmsg)
            Case "elementwaittime_ms"
                If LCase(Trim(annotationValue)) = "default" Then
                    annotationValue = CDbl(TestComplete._DefaultObjectTimeout) / 1000
                End If
                Commands.runCommand("SetElementWaitTime(" + CStr(annotationValue) + ")", ret, errmsg)
            Case Else : Throw (New Exception("Unknown Annonation Name:" + annotationName + " Annontaion Value:" + CStr(annotationValue)))
        End Select
    End Function

    'Public Function SetPageTimeOut(ByVal nPageTimeout)
    '    Dim ret, errmsg
    '    If Commands.Run("SetPageTimeOut(" + nPageTimeout + ")", ret, errmsg) Then
    '        If errmsg = "SUCCESS" Then
    '            SetPageTimeOut = ret
    '        Else
    '            SetPageTimeOut = ret
    '            Throw (New Exception("ERROR:" & errmsg))
    '        End If
    '    Else
    '        Throw (New Exception("ERROR:" & errmsg))
    '    End If
    'End Function

    'Public Function SetElementTimeOut(ByVal nElementTimeout)
    '    Dim ret, errmsg
    '    If Commands.Run("SetElementWaitTime(" + nElementTimeout + ")", ret, errmsg) Then
    '        If errmsg = "SUCCESS" Then
    '            SetElementTimeOut = ret
    '        Else
    '            SetElementTimeOut = ret
    '            Throw (New Exception("ERROR:" & errmsg))
    '        End If
    '    Else
    '        Throw (New Exception("ERROR:" & errmsg))
    '    End If
    'End Function

    Public Function GetObjectID(ByVal sObject)
        Dim aObj
        aObj = Core.GetORObject(sObject)
        If UBound(aObj) <> -1 Then
            GetObjectID = aObj(0)
        Else
            GetObjectID = "" 'need to return empty string when we dont find element in OR
        End If
    End Function

    Public Sub StartListener(ByVal port)
        Commands.StartListener(port)
    End Sub
    Public Function CaptureImage(ByVal sFile)
        Dim ret, errmsg
        If Left(Trim(sFile), 2) = "./" Then
            sFile = _scriptRootPath & "/" & Mid(Trim(sFile), 3)
        End If

        sFile = Trim(Replace(sFile, "/", "\"))
        If Commands.runCommand("captureimage(""" + sFile + """)", ret, errmsg) Then
            If errmsg = "SUCCESS" Then
                CaptureImage = ret
            Else
                CaptureImage = ret
                Throw (New Exception("ERROR:" & errmsg))
            End If
        Else
            Throw (New Exception("ERROR:" & errmsg))
        End If
    End Function

    Public Function ExecuteAction(ByVal sAction, ByVal sObject, ByVal sData, ByVal isObject)
        'MsgBox("Execute Action: " & sAction & " sObject: " & sObject & " sData: " & sData & " isObject:" & isObject)
        If isObject = "true" Then
            Core.bCurrentElementIsObject = True
        Else
            Core.bCurrentElementIsObject = False
        End If
        ExecuteAction = Core.CommandExecutor("signature", sAction, sObject, sData)
    End Function

    Public Sub AddRepository(ByVal sPath)
        Dim oCore As Core : oCore = New Core
        oCore.AddRepository(sPath)
        oCore = Nothing
    End Sub

    Public Sub RemoveRepository_old(ByVal sPath)
        Dim oCore As Core : oCore = New Core
        'oCore.RemoveRepository(sPath)
        'oCore.RemoveRepository(sPath)    Original Statement
        oCore = Nothing
    End Sub
    Public Sub RemoveRepository()
        Dim oCore As Core : oCore = New Core(Core.psRootObject)
        oCore.RemoveRepository()
        oCore = Nothing
    End Sub

    Public Sub KillProcess() 'We have issues with automation agent process while launching qtp using automation object. use this method to kill it on any issues.
        Dim pProcess1() As Process = System.Diagnostics.Process.GetProcessesByName("QTAutomationAgent")
        For Each p As Process In pProcess1
            p.Kill()
        Next
    End Sub

    Public Sub setConfig(ByVal sKey, ByVal sVal)
        oConfigDict.Add(sKey, sVal)
    End Sub

    Private Function getConfigValue(ByVal sKey) As String
        If oConfigDict.Item("is_xAFTCentral") = "true" Then
            On Error Resume Next
            getConfigValue = oConfigDict.Item(sKey).Replace(".\", _scriptRootPath & "\")
            On Error GoTo 0
        Else
            getConfigValue = GetNodeFULL(_scriptRootPath, "//TestEnvironment/" & sKey, 1).GetAttribute("value")
        End If
        getConfigValue = LCase(Trim(getConfigValue))
    End Function

    Private Sub setActionFixture()
        CommonConfig._ActionFixture = _scriptRootPath & "/Library/" & "testCompleteActionFixture.xml"
    End Sub

    Public Sub setActionFixture(ByVal sActionFixturePath As String)
        CommonConfig._ActionFixture = sActionFixturePath
    End Sub

    



    Public Function Initialize(ByVal privateKey, ByVal sRootPath, ByVal appName, ByVal BrowserType, ByVal ORPath)
        _key = privateKey
        _scriptRootPath = sRootPath
        _browserType = BrowserType
        CommonConfig._rootPath = Trim(sRootPath)
        CommonConfig._ListenerPort = CInt(Trim(oConfigDict.Item("ListenerPort")))
        'MsgBox("in dll" & sRootPath)

        oConfigDict.Item("logFile") = oConfigDict.Item("logFile") & ".testComplete"

        If oConfigDict.Item("is_xAFTCentral") = "true" Then
            CommonConfig.isCentral = True
        Else
            CommonConfig.isCentral = False
        End If
        oConfigDict.Item("testcompleteActionFixture") = Commands.resolvePath(oConfigDict.Item("testcompleteActionFixture"))
        setActionFixture(Replace(oConfigDict.Item("testcompleteActionFixture"), "/", "\"))
        ORPath = Commands.resolvePath(oConfigDict.Item("ORPath"))
        _orPath = ORPath
        oConfigDict.Item("Driver") = Commands.resolvePath(oConfigDict.Item("Driver"))
        launchAndRunTestComplete(oConfigDict.Item("Driver"), CommonConfig._ListenerPort)
    End Function

    '@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    'Function Name	: GetNode
    'Purpose		: Get the node using XPath
    '@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@

    Function GetNode(ByVal XPath, ByVal nConfig)
        Dim oFSO
        oFSO = CreateObject("Scripting.FileSystemObject")
        GetNode = GetNodeFULL(oFSO.GetAbsolutePathName("."), XPath, nConfig)
        oFSO = Nothing
    End Function

    Function GetNodeFULL(ByVal rootPath, ByVal XPath, ByVal nConfig)
        Dim sConfigFilePath, xmldoc
        Select Case nConfig
            Case 1
                sConfigFilePath = rootPath & "\configuration\AFTQTPConfig.xml"
            Case 2.0R
                sConfigFilePath = rootPath & "\configuration\AFTTestBatch.xml"
            Case 3
                sConfigFilePath = rootPath & "\configuration\AFTConfig.xml"
            Case 4
                sConfigFilePath = rootPath & "\configuration\AFTTestCompleteConfig.xml"
            Case Else
                sConfigFilePath = rootPath & "\configuration\AFTTestBatch.xml"
        End Select
        xmldoc = CreateObject("Msxml2.DOMDocument.3.0")
        xmldoc.load(sConfigFilePath)
        xmldoc.async = False
        GetNodeFULL = xmldoc.selectSingleNode(XPath)
        xmldoc = Nothing

    End Function
End Class


