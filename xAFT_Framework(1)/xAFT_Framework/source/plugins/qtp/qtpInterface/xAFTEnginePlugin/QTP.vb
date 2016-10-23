Imports System.Runtime.InteropServices
Imports System.Threading
Imports System.IO
Imports System.Net
Imports System.Net.Sockets
Imports System.Configuration
Imports System.Text
Imports System.Security.Cryptography

Public Class QTP
    Dim psRootObject As String
    Dim psCurrentPage As String
    Private tobedeletedPath As String

    Dim _Test As String
    'Public Shared _oExecute ' -This is modified to be used from CommonConfig._ToolInstance
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
    'Public Shared _ActionFixtureXML As String 'Moved this to common config

    Private Const Const_ObjectSyncTimeout_ms = "10000"
    Private Const Const_PageLoadTimeout_ms = "60000"
    Private Const Const_DisableSmartIdentification = "true"
    Private Const Const_ListenerPort = "55555"
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
        libraryList = New List(Of String)
        CommonConfig.executionEngine = "qtp"
    End Sub

    Public Function getpath()
        getpath = tobedeletedPath
    End Function

    Public Sub setpath(ByVal value)
        tobedeletedPath = value
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
            Throw (New Exception("Unable to stop QTP:" & errMsg))
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

        If Trim(sBrowser) = "" Then
            'no browser passed
            GetBrowserVersion = "--"
            Exit Function
        End If
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
                    annotationValue = CDbl(QTP._DefaultObjectTimeout) / 1000
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

    Private Sub setActionFixture()
        CommonConfig._ActionFixture = _scriptRootPath & "/Library/" & "qtpActionFixture.xml"
    End Sub

    Public Sub setActionFixture(ByVal sActionFixturePath As String)
        CommonConfig._ActionFixture = sActionFixturePath
    End Sub

    Public Function getActionCommand(ByVal xAFTObjectType, ByVal sToolObjectType, ByVal xAFTAction, ByVal sActionData)
        getActionCommand = CommonConfig.getActionCommand(xAFTObjectType, sToolObjectType, xAFTAction, sActionData)
    End Function
    

    Public Sub StartListener(ByVal port)

        Commands.StartListener(port)
    End Sub
    Public Function CaptureImage(ByVal sFile)
        Dim ret, errmsg
        sFile = Trim(Replace(sFile, "./", _scriptRootPath & "/"))
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

    Public Function ExecuteAction(ByVal xAFtObjectType, ByVal sAction, ByVal sObject, ByVal sData, ByVal isObject)
        'MsgBox("Execute Action: " & sAction & " sObject: " & sObject & " sData: " & sData & " isObject:" & isObject)
        If isObject = "true" Then
            Core.bCurrentElementIsObject = True
        Else
            Core.bCurrentElementIsObject = False
        End If
        ExecuteAction = Core.AppCommandExecutor(xAFtObjectType, sAction, sObject, sData)
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

    Public Sub setLibrary(ByVal sLibPath As String)
        libraryList.Add(sLibPath)
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

    Public Function Initialize(ByVal privateKey, ByVal sRootPath, ByVal appName, ByVal BrowserType, ByVal ORPath)
   
        ReDim _ORArray(-1)
        KillProcess()
        _key = privateKey
        _scriptRootPath = sRootPath
        _browserType = BrowserType

        CommonConfig._rootPath = Trim(sRootPath)
        oConfigDict.Item("qtpActionFixture") = Commands.resolvePath(oConfigDict.Item("qtpActionFixture"))
        ORPath = Commands.resolvePath(oConfigDict.Item("ORPath"))
        oConfigDict.Item("Driver") = Commands.resolvePath(oConfigDict.Item("Driver"))
        'MsgBox("in dll" & sRootPath)

        If oConfigDict.Item("is_xAFTCentral") = "true" Then
            CommonConfig.isCentral = True
        Else
            CommonConfig.isCentral = False
        End If
        setActionFixture(Replace(oConfigDict.Item("qtpActionFixture"), "./", _scriptRootPath & "/"))
        App = CreateObject("QuickTest.Application")
        Dim oFSO, oEnv, oParams

        'App = CreateObject("QuickTest.Application")
        On Error Resume Next
        If App.Launched Then  'If QTP is already open first it will quit that and launch again
            If Err.Number <> 0 Then
                App.Launch()
            Else
                App.Quit()
                App.Launch()
            End If
        End If
        On Error GoTo 0
        Dim appHide, bDisableSmartIdentification, nPageLoadTimeout, sQTPRunInFastMode

        appHide = getConfigValue("HideQTPWhileExecution")
        If appHide = "" Then appHide = Const_HideQTPWhileExecution
        _HighlightObjects = getConfigValue("HighlightObjects")
        If _HighlightObjects = "" Then _HighlightObjects = Const_HighlightObjects
        _CloseQTPOnCompletion = getConfigValue("CloseQTPOnCompletion")
        If _CloseQTPOnCompletion = "" Then _CloseQTPOnCompletion = Const_CloseQTPOnCompletion
        bDisableSmartIdentification = getConfigValue("DisableSmartIdentification") 'Set the Smart Identification to Disable
        If bDisableSmartIdentification = "" Then bDisableSmartIdentification = Const_DisableSmartIdentification
        nPageLoadTimeout = getConfigValue("PageLoadTimeout_ms") 'Set the Browser Navigation Timeout
        If nPageLoadTimeout = "" Then nPageLoadTimeout = Const_PageLoadTimeout_ms

        'sQTPRunMode = GetNodeFULL(_scriptRootPath, "//TestEnvironment/RunMode", 1).GetAttribute("value")  'Set the Run mode for QTP'
        sQTPRunInFastMode = getConfigValue("RunInFastMode") 'Set the Run mode for QTP
        If sQTPRunInFastMode = "" Then sQTPRunInFastMode = Const_QTPRunInFastMode
        Select Case LCase(Trim(appHide))
            Case "yes", "true", "y"
                App.Visible = False
            Case Else
                App.Visible = True
        End Select

        'sLibraryFolderPath = _scriptRootPath & "\Library\"
        oFSO = CreateObject("Scripting.FileSystemObject")
        'If Not oFSO.FolderExists(sLibraryFolderPath) Then
        '    App.Folders.Add(sLibraryFolderPath, 1)
        'End If
        'Opening the test script
        Dim _Driver = getConfigValue("Driver")

        _Driver = Trim(Replace(_Driver, ".\", _scriptRootPath & "\"))
        'MsgBox("Driver: " & _Driver)
        _port = getConfigValue("ListenerPort")        'get port number to be used by QTP to listen for commands
        CommonConfig._ListenerPort = _port
        If _port = "" Then _port = Const_ListenerPort
        _port = CInt(_port)
        If Right(_Driver, 1) = "\" Then _Driver = Left(_Driver, Len(_Driver) - 1) 'Remove \ if appended at end since the folder itself is QTP test script
        App.Open(_Driver, False)
        'MsgBox("1")

        _Driver = vbNull
        'Configuring QTP settings
        _DefaultObjectTimeout = getConfigValue("ObjectSyncTimeout_ms") 'Set the Object Synchronization Timeout
        If _DefaultObjectTimeout = "" Then _DefaultObjectTimeout = Const_ObjectSyncTimeout_ms
        App.Test.Settings.Run.ObjectSyncTimeOut = _DefaultObjectTimeout
        App.Test.Settings.Run.DisableSmartIdentification = bDisableSmartIdentification 'Set the Smart Identification to Disable
        App.Test.Settings.Web.BrowserNavigationTimeout = nPageLoadTimeout 'Set the Browser Navigation Timeout
        If LCase(Trim(sQTPRunInFastMode)) = "yes" Then
            App.Options.Run.RunMode = "Fast" ' Set the QTP Runmode
        Else
            App.Options.Run.RunMode = "Normal" ' Set the QTP Normal Mode
        End If
        'MsgBox("2")
        'App.SetActiveAddins Array("Web") 'Set the required Add-Ins

        'Remove existing libraries as well as repositories
        App.Test.Settings.Resources.Libraries.RemoveAll()
        App.Test.Actions(1).ObjectRepositories.RemoveAll()
        'MsgBox("3")
        'Adding Library files and OR's to test
        '<#################### LIBRARIES ####################
        Dim _Library = getConfigValue("LibraryPath")
        _Library = Trim(Replace(_Library, ".\", _scriptRootPath & "\"))
        If Right(_Library, 1) <> "\" Then _Library = _Library & "\" 'Add \ at end if not present
        'No need to parse through all file bcoz we know we have only one file called startup.qft
        'Dim oFiles : oFiles = oFSO.GetFolder(_Library & "Default").Files
        'Dim oFile
        'For Each oFile In oFiles 'Load all libraries from default folder under //QTP/Library root folder
        '    If InStr(1, oFile.name, ".qfl", 1) > 0 Then _
        '        App.Test.Settings.Resources.Libraries.Add(_Library & "Default\" & oFile.name)
        'Next
        App.Test.Settings.Resources.Libraries.Add(_Library & "startup.qfl")
        App.Test.Settings.Resources.Libraries.Add(_Library & "Common.qfl")
        'MsgBox("4")

        'Load app specific libraries if configured in external scripts folder. App specific libraries are expected to have same name as application name passed.
        If oConfigDict.Item("is_xAFTCentral") = "true" Then
            'use libraryList
            For Each sFile In libraryList.ToArray
                'Load all libraries from temp location
                App.Test.Settings.Resources.Libraries.Add(sFile)
            Next
        Else
            Dim _resource : _resource = _scriptRootPath
            'MsgBox("4.1 " & _resource)
            _resource = getConfigValue("AppSpecificLibraryPath")
            _resource = Replace(_resource, ".\", _scriptRootPath & "\")

            'MsgBox("4.2 " & _resource)
            Dim oFiles : oFiles = oFSO.GetFolder(_resource).Files
            For Each oFile In oFiles 'Load all libraries from default folder under //QTP/Library root folder
                If InStr(1, oFile.name, ".qfl", 1) > 0 Then _
                    App.Test.Settings.Resources.Libraries.Add(_resource & oFile.name)
            Next
            oFiles = Nothing
        End If
        'MsgBox("5")
        '#################### LIBRARIES ####################>
        '<#################### OR ####################
        'Load app specific OR's. App specific OR's are expected to have same name as application name passed.
        '<TEMP FIX
        oEnv = New QTPConfig() 'initialize environment: 
        ORPath = Replace(ORPath, ".\", _scriptRootPath & "\")
        'MsgBox("6")
        If oConfigDict.Item("is_xAFTCentral") = "true" Then
            oEnv.Init()
            App.Test.Actions(1).ObjectRepositories.Add(QTPConfig.oDict("XAFTCENTRAL_TSR_FILE"))
        Else
            If InStr(1, ORPath, ".xml", 1) > 0 Then ORPath = Left(ORPath, InStrRev(ORPath, "\") - 1)
            '>
            If InStr(1, ORPath, ".tsr", 1) > 0 Then 'File is passed instead of folder
                App.Test.Actions(1).ObjectRepositories.Add(ORPath)
                ReDim _ORArray(0)
                _ORArray(0) = ORPath
            Else

                Dim oFiles : oFiles = oFSO.GetFolder(ORPath).Files
                For Each oFile In oFiles 'Load all libraries from default folder under //QTP/Library root folder
                    If InStr(1, oFile.name, appName, 1) > 0 And InStr(1, oFile.name, ".tsr", 1) > 0 Then
                        ReDim Preserve _ORArray(UBound(_ORArray) + 1)
                        _ORArray(UBound(_ORArray)) = ORPath & "\" & oFile.name
                        App.Test.Actions(1).ObjectRepositories.Add(ORPath & "\" & oFile.name)
                    End If
                Next
                oFiles = Nothing
            End If
            oEnv.Init()
        End If
        '#################### OR ####################>
        oFSO = Nothing


        App.Test.Save()
        'Threading.Thread.Sleep(10000)

        '   oEnv = New Configuration() 'initialize environment: 
        '   oEnv.Init()
        'MsgBox(" QTP._port- " & QTP._port & " logfile- " & Replace(QTPConfig.oDict("logFile"), ".\", _scriptRootPath & "\"))
        oParams = App.Test.ParameterDefinitions.GetParameters()
        oParams.Item("inpkey").value = _key
        oParams.Item("port").value = QTP._port
        Dim logFile = Replace(oConfigDict.Item("logFile"), "./", _scriptRootPath & "\")
        oParams.Item("logFile").value = Replace(logFile, "/", "\") & ".qtp"
        App.Test.Run(, False, oParams)
        Initialize = "Done"
    End Function
    'Public Function RUNQTP(ByRef App)
    '    Dim aa, oParamColl, oParams
    '    oParamColl = App.Test.ParameterDefinitions
    '    oParams = oParamColl.GetParameters() ' Retrieve the Parameters collection defined for the test.

    '    oParams.Item("inp").Value = "Hello" ' Retrieve a specific parameter.
    '    App.Test.Run(, True, oParams) 'Run the test with changed parameters.
    '    'oParams.Item("outp").Value()

    'End Function

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

Friend Class Generic
    Public Sub New()
        'Public constructor
        '// dummy call. just to expose it as com object for now.
    End Sub
    Public testvar
    'Public Shared Function GetObject( _
    ' <OptionalAttribute()> Optional ByVal PathName As String = Nothing, _
    ' <OptionalAttribute()> Optional ByVal Class As String = Nothing _
    ') As Object

    'End Function

    Shared Function FormatDate(ByVal sDate, ByVal sFormat)
        Dim nDay, nMonth, nYr
        sDate = CDate(sDate)
        nDay = Day(sDate)
        If Len(nDay) = 1 Then nDay = 0 & nDay
        nMonth = Month(sDate)
        If Len(nMonth) = 1 Then nMonth = 0 & nMonth
        nYr = Year(sDate)
        Select Case UCase(Trim(sFormat))
            Case "MMDDYYYY"
                FormatDate = nMonth & nDay & nYr
            Case "DDMMYYYY"
                FormatDate = nDay & nMonth & nYr
            Case "MM-DD-YYYY"
                FormatDate = nMonth & "-" & nDay & "-" & nYr
            Case "DD-MM-YYYY"
                FormatDate = nDay & "-" & nMonth & "-" & nYr
            Case "DD-MMM-YYYY"
                FormatDate = nDay & "-" & MonthName(nMonth, True) & "-" & nYr
            Case "DD-MMM-YY"
                FormatDate = nDay & "-" & MonthName(nMonth, True) & "-" & Right(nYr, 2)
            Case Else
                FormatDate = nMonth & nDay & nYr 'Defaults to MMDDYYYY format
        End Select
    End Function
End Class
