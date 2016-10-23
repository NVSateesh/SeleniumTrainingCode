
Public Class QTPConfig
    Public Shared oDict As Object
    Public Shared psORs(-1)
    Public Shared psCommonORXML
    Public Shared psDynamicORXML
    Public Shared psTempFolder
    Public Shared psTargetFolder  ' This statement added to hold the Temp folder path
    Private onlyonce As Boolean


    Public Sub New()

        'Store the Test data excel file path in a environment variable
    End Sub
    Public Sub Init()
        oDict = CreateObject("Scripting.Dictionary")
        ReDim psORs(-1)

        'LoadEnvironment()
        oDict("SOURCE_FOLDER_PATH") = Replace(QTP._scriptRootPath, "\", "/")
        psTempFolder = oDict("SOURCE_FOLDER_PATH")
        oDict("TARGET_FOLDER_PATH") = Replace(System.IO.Path.GetTempPath(), "\", "/") & "XAFT_QTP/" & Generic.FormatDate(System.DateTime.Now, "DD-MMM-YYYY") & "/"
        psTargetFolder = oDict("TARGET_FOLDER_PATH")
        psCommonORXML = oDict("TARGET_FOLDER_PATH") & "CommonOR.XML"
        psDynamicORXML = oDict("TARGET_FOLDER_PATH") & "DynamicOR.XML"
        DeleteOldXAFTResultsFolders()
        'MsgBox("5.1")
        If QTP.oConfigDict.Item("is_xAFTCentral") = "true" Then
            'MsgBox("5.1.1")
            ConvertXMLtoTSR(QTP.oConfigDict.Item("ObjectRepositoryPath").Replace(".\", QTP._scriptRootPath & "\"))
            'MsgBox("5.1.2 " & QTPInterface.oConfigDict.Item("ObjectRepositoryPath"))
        Else
            ExportORs()
            Dim oCore : oCore = New Core
            oCore.GenerateCommonORXML()
            oCore = Nothing
        End If
        'MsgBox("5.2")
    End Sub

    Public Property Environment(ByVal sVal)

        Get
            sVal = Replace(UCase(Trim(sVal)), " ", "_")
            Environment = oDict(sVal)
        End Get

        Set(ByVal value)
            sVal = Replace(UCase(Trim(sVal)), " ", "_")
            oDict(sVal) = value
        End Set
    End Property

    Sub LoadEnvironment()
        Dim oFile, sAttributeValue, sAttributeName, xmldoc, oAttribute
        'Dim oGeneric = CreateObject("QTP.Generic")
        'oFile = CreateObject("Scripting.FileSystemObject")  'File System Object


        oDict("SOURCE_FOLDER_PATH") = Replace(QTP._scriptRootPath, "\", "/")
        oFile = Nothing
        'Loading the Target Folder(Temp Location) in Environment variable
        oDict("TARGET_FOLDER_PATH") = Replace(System.IO.Path.GetTempPath(), "\", "/") & "XAFT_QTP/" & Generic.FormatDate(System.DateTime.Now, "DD-MMM-YYYY") & "/"

        'Settings the Global Variables
        xmldoc = CreateObject("Msxml2.DOMDocument.3.0")
        xmldoc.load(oDict("SOURCE_FOLDER_PATH") & "/configuration/AFTTestBatch.xml")
        xmldoc.async = False
        Dim GetNodes

        GetNodes = xmldoc.selectNodes("//TestBatch/TestSet[@ExecuteSuite='Yes']")
        For Each oAttribute In GetNodes.Item(0).Attributes
            sAttributeName = UCase(Trim(oAttribute.NodeName))
            sAttributeName = (RecursiveFilter(sAttributeName, "  ", " ")).Trim()
            sAttributeName = Replace(sAttributeName, " ", "_")
            sAttributeValue = Trim(Replace(oAttribute.Value, "\", "/")) 'to have standard convetion when dealing with file paths
            If Left(sAttributeValue, 2) = "./" Then
                oDict("VAR_" & sAttributeName) = oDict("SOURCE_FOLDER_PATH") & Mid(sAttributeValue, 2) 'Adding VAR_ to variable to handle restricted naming conventions
            Else
                oDict("VAR_" & sAttributeName) = sAttributeValue
            End If
        Next
        GetNodes = Nothing
        xmldoc = Nothing
        'oGeneric = Nothing
        oAttribute = Nothing
    End Sub

    Function RecursiveFilter(ByVal sData, ByVal sFilter, ByVal sReplaceWith)
        Do
            sData = Replace(sData, sFilter, sReplaceWith, 1, -1, 1)
        Loop Until InStr(1, sData, sFilter, 1) = 0
        RecursiveFilter = Trim(sData)
    End Function

    Sub DeleteOldXAFTResultsFolders()
        Dim ofso, oParentFolder, coSubFoldersColection, sParentFolderPath, oSubfolder
        ofso = CreateObject("Scripting.FileSystemObject")
        sParentFolderPath = ofso.GetParentFolderName(oDict("TARGET_FOLDER_PATH"))
        If ofso.FolderExists(sParentFolderPath) Then
            oParentFolder = ofso.GetFolder(sParentFolderPath)
            If ofso.FolderExists(oParentFolder) Then
                coSubFoldersColection = oParentFolder.SubFolders
                For Each oSubfolder In coSubFoldersColection
                    If CDate(oSubfolder.name) <= DateAdd("d", -2, System.DateTime.Now) Then ofso.DeleteFolder(oSubfolder)
                Next
            End If
            oParentFolder = Nothing
            coSubFoldersColection = Nothing
            'Else		'all files will be overritten while making copies of files to this location at runtime
        End If
        ofso = Nothing
    End Sub

    Function CreateXAFTSubFolderByDate() As Boolean

        CreateXAFTSubFolderByDate = False
        Dim ofso, ofolderObj
        Dim sParentFolder, ochildfolderobj

        ofso = CreateObject("Scripting.FileSystemObject")

        If Not ofso.FolderExists(ofso.GetParentFolderName(oDict("TARGET_FOLDER_PATH"))) Then
            sParentFolder = ofso.GetParentFolderName(oDict("TARGET_FOLDER_PATH"))
            ofolderObj = ofso.CreateFolder(sParentFolder)
            ochildfolderobj = ofso.CreateFolder(oDict("TARGET_FOLDER_PATH"))
            ofolderObj = Nothing
            ochildfolderobj = Nothing
            CreateXAFTSubFolderByDate = True
        Else

            If Not ofso.FolderExists(oDict("TARGET_FOLDER_PATH")) Then
                ofolderObj = ofso.CreateFolder(oDict("TARGET_FOLDER_PATH"))  ''Creates the folder with name 'XAFT_QTP'/todays date in Temp location
                ofolderObj = Nothing
                CreateXAFTSubFolderByDate = True
            Else
                CreateXAFTSubFolderByDate = True
            End If
        End If
        ofso = Nothing
    End Function

    Sub ExportFiles(ByVal sSourceFolderPath)
        Dim oRepositoryUtil, ofso, oParentFolder, cfilescollection, sFileExt, sParentFolder
        Dim sFileName

        If CreateXAFTSubFolderByDate() Then     'This function is used to create sub folder by system date inside XAFT_QTP folder present in Temp location
            oRepositoryUtil = CreateObject("Mercury.ObjectRepositoryUtil")
            If Right(Trim(sSourceFolderPath), 1) = "/" Then
                sParentFolder = sSourceFolderPath
            Else
                sParentFolder = Left(sSourceFolderPath, InStrRev(sSourceFolderPath, "/"))
            End If

            cfilescollection = System.IO.Directory.GetFiles(sParentFolder)
            ofso = CreateObject("Scripting.FileSystemObject")

            For Each file1 In cfilescollection
                If InStr(file1, ".") > 0 Then
                    sFileExt = Right(file1, 3)
                Else
                    sFileExt = ""
                End If
                If sFileExt = "tsr" Then
                    If InStr(ofso.GetFileName(file1), oDict("VAR_APPLICATIONNAME")) > 0 Then
                        sFileName = ofso.GetFileName(file1)
                        If ofso.FileExists(oDict("TARGET_FOLDER_PATH") & Left(sFileName, InStrRev(sFileName, ".") - 1) & ".xml") Then
                            ofso.DeleteFile(oDict("TARGET_FOLDER_PATH") & Left(sFileName, InStrRev(sFileName, ".") - 1) & ".xml")
                            oRepositoryUtil.ExportToXML(file1, oDict("TARGET_FOLDER_PATH") & Left(sFileName, InStrRev(sFileName, ".") - 1) & ".xml")    'Convert the .tsr file into xml
                        Else
                            oRepositoryUtil.ExportToXML(file1, oDict("TARGET_FOLDER_PATH") & Left(sFileName, InStrRev(sFileName, ".") - 1) & ".xml")
                        End If
                        ReDim Preserve psORs(UBound(psORs) + 1) 'Add all the repositories to public array of OR's
                        psORs(UBound(psORs)) = oDict("TARGET_FOLDER_PATH") & Left(sFileName, InStrRev(sFileName, ".") - 1) & ".xml"
                    End If
                ElseIf sFileExt = "xls" Then
                    If InStr(ofso.GetFileName(file1), oDict("VAR_APPLICATIONNAME")) > 0 Then _
                                      System.IO.File.Copy(file1, oDict("TARGET_FOLDER_PATH") & ofso.GetFileName(file1), True) 'copy the xls file from source folder to target folder(temp location
                End If
            Next
            oRepositoryUtil = Nothing
            ofso = Nothing
            oParentFolder = Nothing
            cfilescollection = Nothing
        End If
    End Sub

    Sub ExportORs()
        Dim oRepositoryUtil, ofso
        Dim sFileName

        If CreateXAFTSubFolderByDate() Then     'This function is used to create sub folder by system date inside XAFT_QTP folder present in Temp location
            oRepositoryUtil = CreateObject("Mercury.ObjectRepositoryUtil")
            ofso = CreateObject("Scripting.FileSystemObject")

            For Each file1 In QTP._ORArray
                sFileName = ofso.GetFileName(file1)
                If ofso.FileExists(oDict("TARGET_FOLDER_PATH") & Left(sFileName, InStrRev(sFileName, ".") - 1) & ".xml") Then
                    ofso.DeleteFile(oDict("TARGET_FOLDER_PATH") & Left(sFileName, InStrRev(sFileName, ".") - 1) & ".xml")
                    oRepositoryUtil.ExportToXML(file1, oDict("TARGET_FOLDER_PATH") & Left(sFileName, InStrRev(sFileName, ".") - 1) & ".xml")    'Convert the .tsr file into xml
                Else
                    oRepositoryUtil.ExportToXML(file1, oDict("TARGET_FOLDER_PATH") & Left(sFileName, InStrRev(sFileName, ".") - 1) & ".xml")
                End If
                ReDim Preserve psORs(UBound(psORs) + 1) 'Add all the repositories to public array of OR's
                psORs(UBound(psORs)) = oDict("TARGET_FOLDER_PATH") & Left(sFileName, InStrRev(sFileName, ".") - 1) & ".xml"
            Next
            oRepositoryUtil = Nothing
            ofso = Nothing
        End If
    End Sub

    Sub ConvertXMLtoTSR(ByVal xmlfilePath)
        Dim oRepositoryUtil, ofso
        Dim sFileName, tsrfile As String

        If CreateXAFTSubFolderByDate() Then     'This function is used to create sub folder by system date inside XAFT_QTP folder present in Temp location
            oRepositoryUtil = CreateObject("Mercury.ObjectRepositoryUtil")
            ofso = CreateObject("Scripting.FileSystemObject")
            sFileName = ofso.GetFileName(xmlfilePath)
            'MsgBox("5.3.1")
            'MsgBox(oDict("TARGET_FOLDER_PATH"))

            oDict("XAFTCENTRAL_TSR_FILE") = oDict("TARGET_FOLDER_PATH") & Left(sFileName, InStr(sFileName, ".") - 1) & ".tsr"
            'MsgBox("5.3.2 " & oDict("XAFTCENTRAL_TSR_FILE"))

            If ofso.FileExists(oDict("XAFTCENTRAL_TSR_FILE")) Then
                ofso.DeleteFile(oDict("XAFTCENTRAL_TSR_FILE"))
            End If
            'MsgBox("5.3.3 " & xmlfilePath & "--" & oDict("XAFTCENTRAL_TSR_FILE"))
            ' 
            'tsrfile = oDict("XAFTCENTRAL_TSR_FILE")
            '------------- ISSUE CONVERTING XML TO TSR -----------------------
            oDict("XAFTCENTRAL_TSR_FILE") = xmlfilePath 'Modified code to use tsr file directly; user is expected to upload tsr instead of xml

            'On Error Resume Next
            'oRepositoryUtil.ImportFromXML(xmlfilePath, tsrfile)    'Convert the .tsr file into xml
            'If Err.Number <> 0 Then
            'MsgBox(Err.Description)
            'End If
            'On Error GoTo 0
            '------------- ISSUE CONVERTING XML TO TSR -----------------------

            oRepositoryUtil = Nothing
            ofso = Nothing
        End If
        'MsgBox("5.3.4")
    End Sub

End Class

