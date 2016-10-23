Imports System.IO
Imports System.Net
Imports System.Runtime.InteropServices
Imports System.Net.Sockets
Imports System.Configuration
Imports System.Text


Friend Class Core
    Public Shared psRootObject As String
    Public Shared psCurrentPage As String
    Public Shared psLastObject As String
    Public Shared psParentRootObject As String
    Public Shared psGenerateDynamicObjectTagFlag As String
    Public Shared bCurrentElementIsObject As Boolean
    'Public Shared bOpenBrowser As String
    Public Shared paoTestData(-1), psTestDataSheets(-1)



    Public Sub New()
        'Dummy call to expose this as com object
        psRootObject = ""
    End Sub

    Public Sub New(ByVal psRootObj As String)
        'Dummy call to expose this as com object
        If psRootObj = "" Then
            psRootObject = ""
        Else
            psRootObject = psRootObj
        End If
    End Sub



    Public Shared Function GetORObject_Full(ByVal oObjectName As String, ByVal sXMLORPath As String)  'Return heirarchy from common xml; retained old logic of returning collection of objects so tht it doesnt break in integration
        Dim oNodeCollection, oNode, sHierarchy
        Dim aoORObjects(-1) ', sXML

        Dim xmldoc As Object
        xmldoc = CreateObject("Msxml2.DOMDocument.3.0")
        xmldoc.setProperty("SelectionLanguage", "XPath")
        xmldoc.load(sXMLORPath)
        xmldoc.async = False
        oNodeCollection = xmldoc.selectNodes("//or[@active='yes']/element[translate(@elementName, 'abcdefghijklmnopqrstuvwxyz', 'ABCDEFGHIJKLMNOPQRSTUVWXYZ')='" & UCase(Trim(oObjectName)) & "']") 'Get the object collection using XPath

        For Each oNode In oNodeCollection
            sHierarchy = oNode.Text
            psRootObject = Left(sHierarchy, InStr(sHierarchy, """)") + 2)
            If InStr(1, sHierarchy, "Page(""", 1) > 0 Then _
                psCurrentPage = Left(sHierarchy, InStr(InStr(1, sHierarchy, "Page(""", 1), sHierarchy, """)", 1) + 2)
            ReDim Preserve aoORObjects(UBound(aoORObjects) + 1)
            aoORObjects(UBound(aoORObjects)) = oNode.Text 'Evaluate the object hierarchy string gives the object
        Next
        xmldoc = Nothing
        GetORObject_Full = aoORObjects
    End Function

    Public Shared Function GetORObject(ByVal oObjectName As String)  'Return heirarchy from common xml; retained old logic of returning collection of objects so tht it doesnt break in integration
        If CommonConfig.isCentral Or CommonConfig.executionEngine = "testComplete" Then
            'When xAFT Central, set object states to be used while performing object actions. Set these only when passed element if found in repository
            Dim aoObject(-1)
            If bCurrentElementIsObject Then
                psLastObject = psRootObject
                psRootObject = Left(oObjectName, InStr(oObjectName, ")"))

                If oObjectName.Contains("Page(") Then
                    'This is a web app and has a page in the hierarchy
                    psCurrentPage = Left(oObjectName, InStr(InStr(oObjectName, "Page"), oObjectName, ")", 1))
                Else
                    psCurrentPage = ""
                End If

                ReDim aoObject(0)
                aoObject(0) = oObjectName
            End If
            GetORObject = aoObject
        Else
            GetORObject = GetORObject_Full(oObjectName, QTPConfig.psDynamicORXML)
            If UBound(GetORObject) = -1 Then GetORObject = GetORObject_Full(oObjectName, QTPConfig.psCommonORXML)
        End If
    End Function

    Public Shared Function TC_GetKeyword(ByVal sObjectName)
        Dim aoObject(0)

        aoObject(0) = Trim(TC_GetORObject_Full(TestComplete._orPath, sObjectName))
        If aoObject(0) = "" Then ReDim aoObject(-1)
        TC_GetKeyword = aoObject
    End Function

    Public Shared Function TC_GetORObject_Full(ByVal sXML, ByVal sObjectName)
        Dim sNodeName, oNode
        Dim xmldoc
        sNodeName = ""
        xmldoc = CreateObject("Msxml2.DOMDocument.3.0")
        xmldoc.setProperty("SelectionLanguage", "XPath")
        xmldoc.load(sXML)
        xmldoc.async = False
        TC_GetORObject_Full = TC_getHierarchy(xmldoc, sObjectName)
        'GetORObject_Full = Eval(GetHierarchy(xmldoc, sObjectName))
        xmldoc = Nothing
    End Function

    Public Shared Function TC_getHierarchy(ByVal xmldoc, ByVal sObjectName)
        Dim oNode, sParent

        oNode = xmldoc.selectSingleNode("//Element[@name=""" + sObjectName + """]/id")
        sParent = oNode.parentNode.GetAttribute("parent")
        If sParent <> "" Then
            TC_getHierarchy = TC_getHierarchy(xmldoc, sParent) & "." & oNode.text
        Else
            TC_getHierarchy = oNode.text
        End If
        'EVal(TC_getHierarchy).Refresh()

    End Function


    'Public Shared Function GetORObject_OLD(ByVal oObjectName As String)  '\\Returns  collection of OR objects with the name passed as param
    '    Dim oNodeCollection, oNode, sHierarchy, bPageFound, sTempPage
    '    Dim aoORObjects(-1)

    '    Dim xmldoc As Object
    '    sTempPage = ""
    '    For Each sXML In Configuration.psORs
    '        'MsgBox(sXML)
    '        xmldoc = CreateObject("Msxml2.DOMDocument.3.0")
    '        xmldoc.setProperty("SelectionLanguage", "XPath")
    '        xmldoc.setProperty("SelectionNamespaces", "xmlns:qtpRep='http://www.mercury.com/qtp/ObjectRepository'")

    '        xmldoc.load(sXML)

    '        xmldoc.async = False
    '        oNodeCollection = xmldoc.selectNodes("//qtpRep:Object[translate(@Name, 'abcdefghijklmnopqrstuvwxyz', 'ABCDEFGHIJKLMNOPQRSTUVWXYZ')='" & UCase(Trim(oObjectName)) & "']") 'Get the object collection using XPath


    '        bPageFound = False 'It will be set to true if current object has page in its hierarchy
    '        'MsgBox(oObjectName)
    '        For Each oNode In oNodeCollection
    '            sHierarchy = ""
    '            Do
    '                If (oNode.GetAttribute("Class").ToString() <> "") Then 'And oNode.GetAttribute("Class") IsNot "" Then
    '                    sHierarchy = oNode.GetAttribute("Class") & "(""" & oNode.GetAttribute("Name") & """)" & "." & sHierarchy   'Build the object hierarchy
    '                    sTempPage = oNode.GetAttribute("Class") & "(""" & oNode.GetAttribute("Name") & """)" & "." & sTempPage 'Build the object hierarchy
    '                End If
    '                If LCase(Trim(oNode.GetAttribute("Class").ToString())) = "browser" Then
    '                    psRootObject = oNode.GetAttribute("Class") & "(""" & oNode.GetAttribute("Name") & """)"
    '                    Exit Do
    '                ElseIf LCase(Trim(oNode.GetAttribute("Class").ToString())) = "page" Then
    '                    oNode = oNode.parentNode
    '                    bPageFound = True
    '                Else
    '                    oNode = oNode.parentNode
    '                    If oNode.GetAttribute("Name").ToString() <> "" Then
    '                        psRootObject = oNode.GetAttribute("Class") & "(""" & oNode.GetAttribute("Name") & """)"
    '                        If Not bPageFound Then sTempPage = ""
    '                    End If
    '                End If
    '            Loop Until oNode.nodeName = "qtpRep:Objects"
    '            sHierarchy = Trim(sHierarchy)
    '            sHierarchy = Left(sHierarchy, Len(sHierarchy) - 1)
    '            If bPageFound Then 'If page is not found, it will have previously used page as current page
    '                sTempPage = Trim(sTempPage)
    '                psCurrentPage = Left(sTempPage, Len(sTempPage) - 1)
    '            End If
    '            ReDim Preserve aoORObjects(UBound(aoORObjects) + 1)
    '            aoORObjects(UBound(aoORObjects)) = sHierarchy 'Evaluate the object hierarchy string gives the object
    '        Next
    '        If UBound(aoORObjects) > -1 Then Exit For 'no need to check for object in other OR as it is already found
    '    Next
    '    'MsgBox(">>>>" & UBound(aoORObjects))
    '    GetORObject_OLD = aoORObjects
    'End Function

    'Public Function GetActiveScenarios(ByVal path, ByVal sheet, ByVal headerRow) 'returns 2D array holding active scenarios and its description. If UBound(GetActiveScenarios, 2) is -1 it indicates no active scenarios
    '    Dim oExcel, aTemp
    '    Dim aActiveScenarios(1, -1)
    '    oExcel = CreateObject("com.ags.aft.Engine.QTP.Excel")
    '    'oExcel.Load(Environment("TARGET_FOLDER_PATH") & Mid(Environment("VAR_TESTSUITEPATH"), InStrRev(Environment("VAR_TESTSUITEPATH"), "/") + 1), Environment("VAR_SCENARIOSSHEETNAME"), 2)
    '    oExcel.Load(path, sheet, headerRow)
    '    Dim asColArray(2)
    '    asColArray(0) = "Business Scenario Id"
    '    asColArray(1) = "Business Scenario Description"
    '    asColArray(2) = "Execution Flag"

    '    Do
    '        oExcel.SetNextRow()
    '        aTemp = oExcel.GetData(asColArray) 'Returns Data Array of columns passes
    '        If Trim(LCase(aTemp(2))) = "y" Then 'Execution flag
    '            'Extract Scenario details only when execution flag is 'Y'
    '            ReDim Preserve aActiveScenarios(1, UBound(aActiveScenarios, 2) + 1)
    '            aActiveScenarios(0, UBound(aActiveScenarios, 2)) = aTemp(0)
    '            aActiveScenarios(1, UBound(aActiveScenarios, 2)) = aTemp(1)
    '        End If
    '        Erase aTemp
    '        '    Loop Until oExcel.CurrentRow = oExcel.LastRow
    '        '    oExcel = Nothing
    '        '    GetActiveScenarios = aActiveScenarios 'Returns array which holds scenarioID and its details
    '        'End Function

    '        'Function Arr(ByVal a2D_Array, ByVal nDimention) 'This is for lazy people like me who need 1 dimention array from 2d array; 
    '        '    'Eg usage: For Each step1 in Arr(GetActiveScenarios , 0)
    '        '    Dim nIndex
    '        '    Dim aTemp(-1)

    '        '    nIndex = 0
    '        '    For nIndex = 0 To UBound(a2D_Array, 2)
    '        '        ReDim Preserve aTemp(UBound(aTemp) + 1)
    '        '        aTemp(UBound(aTemp)) = a2D_Array(nDimention, nIndex)
    '        '    Next
    '        '    Arr = aTemp
    '        '    Erase aTemp
    '        'End Function

    Function XAFT_FilterSymbols(ByVal sData)
        If Left(Trim(sData), 1) = "~" Then sData = Mid(Trim(sData), 2)
        If Right(Trim(sData), 1) = "~" Then sData = Left(Trim(sData), Len(Trim(sData)) - 1)
        XAFT_FilterSymbols = Trim(Replace(sData, vbLf, ""))
    End Function

    '*********************************************************************************************************************************
    'Function Name      :    AddRepository
    'Purpose            :	 Adds repository to ccurrent opened test
    'Parameteres		:	
    'Author				: 	  Pradeep Buddaraju
    'Date				:	  10-July-2012
    '*********************************************************************************************************************************
    Function AddRepository(ByVal sPath)

        Dim sName, xmlEquvalentOfTSR
        sPath = Trim(Replace(sPath, ".\", QTP._scriptRootPath & "/"))
        sPath = Trim(Replace(sPath, "/", "\"))
        QTP.App.Test.Actions(1).ObjectRepositories.Add(sPath)
        'Convert it to xml equivalent
        xmlEquvalentOfTSR = ConvertTSRtoXML(sPath)
        UpdateDynamicORXML(xmlEquvalentOfTSR)
        sName = Mid(sPath, InStrRev(sPath, "\") + 1)
        If CheckRepository(sPath) = 0 Then
            AddRepository = False
        Else
            AddRepository = True
        End If

    End Function
    Public Function UpdateDynamicORXML(ByVal sXML As String) 'This is called when we have loading of OR at runtime
        Dim oNodeCollection, oNode, bPageFound, sTempPage
        Dim aoORObjects(-1), bDuplicate
        Dim oDynamicORXml : oDynamicORXml = CreateObject("Msxml2.DOMDocument.3.0")
        Dim oRoot, oChild, oXMLGroupNode, asDuplicateElements(-1), sDuplicateElements, oDuplicateNode, bDynamicORXMLFile, sNodeName

        If File.Exists(QTPConfig.psDynamicORXML) Then
            oDynamicORXml.load(QTPConfig.psDynamicORXML) 'Load Dynamic OR for update
            oRoot = oDynamicORXml.documentElement

            Dim xmldoc As Object
            sNodeName = ""
            sTempPage = ""
            xmldoc = CreateObject("Msxml2.DOMDocument.3.0")
            xmldoc.setProperty("SelectionLanguage", "XPath")
            xmldoc.setProperty("SelectionNamespaces", "xmlns:qtpRep='http://www.mercury.com/qtp/ObjectRepository'")
            xmldoc.load(sXML)
            xmldoc.async = False

            oNodeCollection = xmldoc.selectNodes("//qtpRep:Object") 'Get the object collection using XPath
            oXMLGroupNode = oDynamicORXml.createElement("or")
            oXMLGroupNode.setAttribute("ref", sXML)
            oXMLGroupNode.setAttribute("ref1", "LoadObjectRepository_Action")
            oXMLGroupNode.setAttribute("active", "yes")

            bPageFound = False 'It will be set to true if current object has page in its hierarchy
            'MsgBox(oObjectName)
            sDuplicateElements = ""
            For Each oNode In oNodeCollection
                sNodeName = UCase(Trim(oNode.GetAttribute("Name")))
                oDuplicateNode = oDynamicORXml.SelectSingleNode("//or[@active='yes']/element[@elementName='" & sNodeName & "']")
                If oDuplicateNode Is Nothing Then
                    'its not duplicate
                    oChild = oDynamicORXml.createElement("element")
                    oChild.setAttribute("elementName", sNodeName)
                    bDuplicate = False 'This is set to true if any duplidates in below function call
                    oChild.text = GetHierarchy(sXML, oNode.GetAttribute("Name"), bDuplicate)
                    If bDuplicate Then 'duplicate with element from same OR
                        'list collection of duplicate elements. throw exceptions once collecting all items
                        sDuplicateElements = sDuplicateElements & oNode.GetAttribute("Name") & vbLf
                    Else
                        oXMLGroupNode.appendChild(oChild)
                    End If
                Else
                    'duplicate with element from different OR
                    sDuplicateElements = sDuplicateElements & oNode.GetAttribute("Name") & "<also found in OR " & GetTSREquivalentForXMLOR(oDuplicateNode.parentNode.GetAttribute("ref")) & ">" & vbLf
                End If
                oChild = Nothing
            Next
            oRoot.appendChild(oXMLGroupNode)
        Else
            ''''''''''''''''''''''Done steps ''''''''''''''''''
            oDynamicORXml.appendChild(oDynamicORXml.createElement("root"))
            oRoot = oDynamicORXml.documentElement
            ''''''''''3333333333333333333333333333333333333333
            Dim xmldoc As Object
            sNodeName = ""
            sTempPage = ""
            xmldoc = CreateObject("Msxml2.DOMDocument.3.0")
            xmldoc.setProperty("SelectionLanguage", "XPath")
            xmldoc.setProperty("SelectionNamespaces", "xmlns:qtpRep='http://www.mercury.com/qtp/ObjectRepository'")
            xmldoc.load(sXML)
            xmldoc.async = False

            oNodeCollection = xmldoc.selectNodes("//qtpRep:Object") 'Get the object collection using XPath
            oXMLGroupNode = oDynamicORXml.createElement("or")
            oXMLGroupNode.setAttribute("ref", sXML)
            oXMLGroupNode.setAttribute("ref1", "LoadObjectRepository_Action")
            oXMLGroupNode.setAttribute("active", "yes")

            bPageFound = False 'It will be set to true if current object has page in its hierarchy
            'MsgBox(oObjectName)
            sDuplicateElements = ""
            For Each oNode In oNodeCollection
                sNodeName = UCase(Trim(oNode.GetAttribute("Name")))
                oDuplicateNode = oDynamicORXml.SelectSingleNode("//or[@active='yes']/element[@elementName='" & sNodeName & "']")
                If oDuplicateNode Is Nothing Then
                    'its not duplicate
                    oChild = oDynamicORXml.createElement("element")
                    oChild.setAttribute("elementName", sNodeName)
                    bDuplicate = False 'This is set to true if any duplidates in below function call
                    oChild.text = GetHierarchy(sXML, oNode.GetAttribute("Name"), bDuplicate)
                    If bDuplicate Then 'duplicate with element from same OR
                        'list collection of duplicate elements. throw exceptions once collecting all items
                        sDuplicateElements = sDuplicateElements & oNode.GetAttribute("Name") & vbLf
                    Else
                        oXMLGroupNode.appendChild(oChild)
                    End If
                Else
                    'duplicate with element from different OR
                    sDuplicateElements = sDuplicateElements & oNode.GetAttribute("Name") & "<also found in OR " & GetTSREquivalentForXMLOR(oDuplicateNode.parentNode.GetAttribute("ref")) & ">" & vbLf
                End If
                oChild = Nothing
            Next
            oRoot.appendChild(oXMLGroupNode)
        End If
        'oRoot.appendChild(oXMLGroupNode)
        oDynamicORXml.save(QTPConfig.psDynamicORXML)
    End Function
    '*********************************************************************************************************************************
    'Function Name      :    AddRepository
    'Purpose            :	 Adds repository to ccurrent opened test
    'Parameteres		:	
    'Author				: 	  Pradeep Buddaraju
    'Date				:	  10-July-2012
    '*********************************************************************************************************************************
    Function AddRepository_old(ByVal sPath)
        Dim sName, xmlEquvalentOfTSR
        QTP.App.Test.Actions(1).ObjectRepositories.Add(sPath)

        'Convert it to xml equivalent
        xmlEquvalentOfTSR = ConvertTSRtoXML(sPath)
        UpdateCommonORXML(xmlEquvalentOfTSR)
        sName = Mid(sPath, InStrRev(sPath, "\") + 1)

        If CheckRepository(sName) = 0 Then
            'AddRepository = False
        Else
            'AddRepository = True
        End If

    End Function
    Function RemoveRepository()
        If File.Exists(QTPConfig.psDynamicORXML) Then File.Delete(QTPConfig.psDynamicORXML)
        Core.psGenerateDynamicObjectTagFlag = ""
    End Function
    Function RemoveRepository_old(ByVal sName)
        Dim nPos
        nPos = QTP.App.Test.Actions(1).ObjectRepositories.Find(sName)
        QTP.App.Test.Actions(1).ObjectRepositories.Remove(nPos)
        RemoveFromCommonORXML(GetXMLEquivalentOfTSROR(sName))
        'set active attribute to "no"
        If CheckRepository(sName) = 0 Then
            RemoveRepository_old = True
        Else
            RemoveRepository_old = False
        End If
    End Function
    Function CheckRepository(ByVal sName)
        CheckRepository = QTP.App.Test.Actions(1).ObjectRepositories.Find(sName)
    End Function
    '*********************************************************************************************************************************
    'Function Name      :    AddItemToRepository
    'Purpose            :	 Adds repository to ccurrent opened test
    'Parameteres		:	
    'Author				: 	  Pradeep Buddaraju
    'Date				:	  09-Aug-2012
    '*********************************************************************************************************************************
    Private Shared Function AddItemToRepository(ByVal elementName As String, ByVal elementValue As String) 'This is called when we have loading of OR at runtime
        Dim oDynamicORXml : oDynamicORXml = CreateObject("Msxml2.DOMDocument.3.0")
        Dim oRoot, oChild, oXMLGroupNode, bDynamicORXMLFile, oDuplicateNode
        If File.Exists(QTPConfig.psDynamicORXML) Then
            oDynamicORXml.load(QTPConfig.psDynamicORXML)
            oRoot = oDynamicORXml.documentElement
            oDynamicORXml.setProperty("SelectionLanguage", "XPath")
            If Core.psGenerateDynamicObjectTagFlag = "" Then
                oXMLGroupNode = oDynamicORXml.createElement("or")
                oXMLGroupNode.setAttribute("ref", "Generate Dynamic Object")
                oXMLGroupNode.setAttribute("active", "yes")
                Core.psGenerateDynamicObjectTagFlag = True
            Else
                oXMLGroupNode = oDynamicORXml.SelectSingleNode("//or[@active='yes' and @ref='Generate Dynamic Object']")
            End If


            'oXMLGroupNode = oDynamicORXml.createElement("or")
            'oXMLGroupNode.setAttribute("ref", "Generate Dynamic Object")
            'oXMLGroupNode.setAttribute("active", "yes")


            'oXMLGroupNode = oDynamicORXml.SelectSingleNode("//or[@active='yes']")
            'bDynamicORXMLFile = True
        Else
            oDynamicORXml.appendChild(oDynamicORXml.createElement("root"))
            oRoot = oDynamicORXml.documentElement
            oXMLGroupNode = oDynamicORXml.createElement("or")
            oXMLGroupNode.setAttribute("ref", "Generate Dynamic Object")
            oXMLGroupNode.setAttribute("active", "yes")
        End If
        oDuplicateNode = oDynamicORXml.SelectSingleNode("//or[@active='yes']/element[@elementName='" & elementName & "']")
        If oDuplicateNode Is Nothing Then
            oChild = oDynamicORXml.createElement("element")
            oChild.setAttribute("elementName", elementName)
            oChild.text = Trim(elementValue)
            oXMLGroupNode.appendChild(oChild)
            oRoot.appendChild(oXMLGroupNode)
            'If Not (bDynamicORXMLFile) Then
            '    oRoot.appendChild(oXMLGroupNode)
            'End If
            oDynamicORXml.save(QTPConfig.psDynamicORXML)
            oChild = Nothing
        End If
    End Function

    Private Shared Function AddItemToRepository_old30Sep2012(ByVal elementName As String, ByVal elementValue As String) 'This is called when we have loading of OR at runtime
        Dim bDuplicate
        Dim oCommonORXml : oCommonORXml = CreateObject("Msxml2.DOMDocument.3.0")
        Dim oRoot, oChild, oXMLGroupNode, asDuplicateElements(-1), sDuplicateElements, oDuplicateNode
        oCommonORXml.load(QTPConfig.psCommonORXML) 'Load common OR for update
        oRoot = oCommonORXml.documentElement

        'MsgBox(sXML)
        oXMLGroupNode = oCommonORXml.createElement("or")
        oXMLGroupNode.setAttribute("ref", "Generate Dynamic Object")
        oXMLGroupNode.setAttribute("active", "yes")

        sDuplicateElements = ""
        oDuplicateNode = oCommonORXml.SelectSingleNode("//or[@active='yes']/element[@elementName='" & elementName & "']")
        If oDuplicateNode Is Nothing Then
            'its not duplicate

            oChild = oCommonORXml.createElement("element")
            oChild.setAttribute("elementName", elementName)
            bDuplicate = False 'This is set to true if any duplidates in below function call
            oChild.text = Trim(elementValue)
            If bDuplicate Then 'duplicate with element from same OR
                'list collection of duplicate elements. throw exceptions once collecting all items
                sDuplicateElements = sDuplicateElements & elementName & vbLf
            Else
                oXMLGroupNode.appendChild(oChild)
            End If
        Else
            'duplicate with element from different OR
            sDuplicateElements = "Add new item to OR:" & sDuplicateElements & "<also found in OR " & elementName & ">" & vbLf
        End If
        oChild = Nothing
        oRoot.appendChild(oXMLGroupNode)
        If sDuplicateElements <> "" Then
            'found duplicates
            ReDim Preserve asDuplicateElements(UBound(asDuplicateElements) + 1)
            asDuplicateElements(UBound(asDuplicateElements)) = "Generate Dynamic object: " & vbLf & " Elements:" & vbLf & sDuplicateElements
        End If

        If UBound(asDuplicateElements) <> -1 Then
            'found some duplidates. Raise exception
            Throw (New Exception("DUPLICATE ELEMENTS FOUND IN OR:" & vbLf & Join(asDuplicateElements, vbLf)))
        End If
        oCommonORXml.save(QTPConfig.psCommonORXML)
    End Function

    Function ConvertTSRtoXML(ByVal sTSRFile As String) 'returns xml equivalent of tsr
        Dim oRepositoryUtil, ofso
        Dim sFileName

        'Add repository path to global list of TSR files loaded
        ReDim Preserve QTP._ORArray(UBound(QTP._ORArray) + 1)
        QTP._ORArray(UBound(QTP._ORArray)) = sTSRFile

        oRepositoryUtil = CreateObject("Mercury.ObjectRepositoryUtil")
        ofso = CreateObject("Scripting.FileSystemObject")
        sFileName = ofso.GetFileName(sTSRFile)

        If ofso.FileExists(QTPConfig.psTargetFolder & Left(sFileName, InStrRev(sFileName, ".") - 1) & ".xml") Then
            ofso.DeleteFile(QTPConfig.psTargetFolder & Left(sFileName, InStrRev(sFileName, ".") - 1) & ".xml")
            oRepositoryUtil.ExportToXML(sTSRFile, QTPConfig.psTargetFolder & Left(sFileName, InStrRev(sFileName, ".") - 1) & ".xml")    'Convert the .tsr file into xml
        Else
            oRepositoryUtil.ExportToXML(sTSRFile, QTPConfig.psTargetFolder & Left(sFileName, InStrRev(sFileName, ".") - 1) & ".xml")
        End If


        'If ofso.FileExists(Configuration.psTempFolder & Left(sFileName, InStrRev(sFileName, ".") - 1) & ".xml") Then
        '    ofso.DeleteFile(Configuration.psTempFolder & Left(sFileName, InStrRev(sFileName, ".") - 1) & ".xml")
        '    oRepositoryUtil.ExportToXML(sTSRFile, Configuration.psTempFolder & Left(sFileName, InStrRev(sFileName, ".") - 1) & ".xml")    'Convert the .tsr file into xml
        'Else
        '    oRepositoryUtil.ExportToXML(sTSRFile, Configuration.psTempFolder & Left(sFileName, InStrRev(sFileName, ".") - 1) & ".xml")
        'End If
        ReDim Preserve QTPConfig.psORs(UBound(QTPConfig.psORs) + 1) 'Add all the repositories to public array of OR's
        QTPConfig.psORs(UBound(QTPConfig.psORs)) = QTPConfig.psTargetFolder & Left(sFileName, InStrRev(sFileName, ".") - 1) & ".xml"
        oRepositoryUtil = Nothing
        ofso = Nothing
        ConvertTSRtoXML = QTPConfig.psTargetFolder & Left(sFileName, InStrRev(sFileName, ".") - 1) & ".xml"
    End Function

    Function ConvertTSRtoXML_old(ByVal sTSRFile As String) 'returns xml equivalent of tsr
        Dim oRepositoryUtil, ofso
        Dim sFileName

        'Add repository path to global list of TSR files loaded
        ReDim Preserve QTP._ORArray(UBound(QTP._ORArray) + 1)
        QTP._ORArray(UBound(QTP._ORArray)) = sTSRFile

        oRepositoryUtil = CreateObject("Mercury.ObjectRepositoryUtil")
        ofso = CreateObject("Scripting.FileSystemObject")
        sFileName = ofso.GetFileName(sTSRFile)

        If ofso.FileExists(QTPConfig.psTargetFolder & Left(sFileName, InStrRev(sFileName, ".") - 1) & ".xml") Then
            ofso.DeleteFile(QTPConfig.psTargetFolder & Left(sFileName, InStrRev(sFileName, ".") - 1) & ".xml")
            oRepositoryUtil.ExportToXML(sTSRFile, QTPConfig.psTargetFolder & Left(sFileName, InStrRev(sFileName, ".") - 1) & ".xml")    'Convert the .tsr file into xml
        Else
            oRepositoryUtil.ExportToXML(sTSRFile, QTPConfig.psTargetFolder & Left(sFileName, InStrRev(sFileName, ".") - 1) & ".xml")
        End If


        'If ofso.FileExists(Configuration.psTempFolder & Left(sFileName, InStrRev(sFileName, ".") - 1) & ".xml") Then
        '    ofso.DeleteFile(Configuration.psTempFolder & Left(sFileName, InStrRev(sFileName, ".") - 1) & ".xml")
        '    oRepositoryUtil.ExportToXML(sTSRFile, Configuration.psTempFolder & Left(sFileName, InStrRev(sFileName, ".") - 1) & ".xml")    'Convert the .tsr file into xml
        'Else
        '    oRepositoryUtil.ExportToXML(sTSRFile, Configuration.psTempFolder & Left(sFileName, InStrRev(sFileName, ".") - 1) & ".xml")
        'End If
        ReDim Preserve QTPConfig.psORs(UBound(QTPConfig.psORs) + 1) 'Add all the repositories to public array of OR's
        QTPConfig.psORs(UBound(QTPConfig.psORs)) = QTPConfig.psTempFolder & Left(sFileName, InStrRev(sFileName, ".") - 1) & ".xml"
        oRepositoryUtil = Nothing
        ofso = Nothing
        ConvertTSRtoXML_old = QTPConfig.psTempFolder & Left(sFileName, InStrRev(sFileName, ".") - 1) & ".xml"
    End Function

    Shared Function CommandExecutor(ByVal xAFTObject, ByVal sAction, ByVal sObject, ByVal sData)
        'MsgBox("AppStepExecutor: " & sAction & ":" & sObject)
        Dim sActionName, aoObject, oObject, arrColumNames, columindexFlag, arrAllDropdownItems, arrVerifyItemsInDropDown, nItemCountFlag
        Dim ret, errMsg, sTemp, RowId, ColId, arrItemsToSelect ', psParentRootObject
        Dim qtpObject, sCommand As String

        CommandExecutor = False
        sActionName = LCase(Trim(Replace(sAction, "~", "")))
        If UCase(Trim(Left(sAction, 7))) = "EXTLIB:" Then
            'This is qtp external method call
            sAction = Trim(Mid(sAction, 8))
            'Call this method;
        End If
        ret = ""
        errMsg = ""
        'MsgBox("CommandExecutor")
        Select Case sActionName
            Case "open"
                sObject = LCase(Trim(sObject))
                If sObject = "" Or sObject = "novalue" Then sObject = QTP._browserType 'nothing passed? use global browser var set at initialize method
                Select Case sObject
                    Case "firefox", "*firefox", "ff", "*ff"
                        If psRootObject <> "" Then
                            Commands.runCommand("Browser(""index:=0"").Navigate(""" & Commands.CustomEVal(sData) & """)", ret, errMsg)
                        Else
                            Commands.runCommand("SystemUtil.Run(""firefox"", """ & Commands.CustomEVal(sData) & """)", ret, errMsg)
                        End If
                    Case "*chrome", "chrome", "*gc", "gc", "googlechrome", "*googlechrome", "google chrome", "*google chrome"
                        If psRootObject <> "" Then
                            Commands.runCommand("Browser(""index:=0"").Navigate(""" & Commands.CustomEVal(sData) & """)", ret, errMsg)
                        Else
                            Commands.runCommand("SystemUtil.Run(""chrome"", """ & Commands.CustomEVal(sData) & """)", ret, errMsg)
                        End If
                    Case Else
                        If psRootObject <> "" Then
                            Commands.runCommand("Browser(""index:=0"").Navigate(""" & Commands.CustomEVal(sData) & """)", ret, errMsg)
                        Else
                            Commands.runCommand("SystemUtil.Run(""iexplore"", """ & Commands.CustomEVal(sData) & """)", ret, errMsg)
                        End If
                End Select
                CommandExecutor = ret
            Case "openapp", "openapplication", "open application", "open app"
                Commands.runCommand("SystemUtil.Run(""" & Commands.CustomEVal(sData) & """)", ret, errMsg)
            Case "methodcall" ' When u call some User defined functions
                'MsgBox("methodCall- " + sData)
                Dim sDataArray, buildDataParams
                Dim sDataType
                sDataArray = Split(sData, ",")
                For i = 0 To UBound(sDataArray)
                    sDataType = "String"
                    If InStr(sDataArray(i), "::") > 0 Then
                        sDataType = LCase(Trim(Left(sDataArray(i), InStr(sDataArray(i), "::") - 1)))
                        If sDataType = "int" Or sDataType = "float" Then
                            sDataType = "notString"
                        End If
                    End If
                    If sDataType = "notString" Then
                        sDataArray(i) = Trim(sDataArray(i))
                    Else
                        sDataArray(i) = """" + Trim(sDataArray(i)) + """"
                    End If
                Next
                Commands.runCommand(sObject + "(" + Join(sDataArray, ",") + ")", ret, errMsg)
                CommandExecutor = ret
            Case "openwindow", "open window"
                If QTP._browserType = "firefox" Or QTP._browserType = "*firefox" Or QTP._browserType = "ff" Or QTP._browserType = "*ff" Then
                    Commands.runCommand("SystemUtil.Run(""firefox" & """,""" & Commands.CustomEVal(sObject) & """)", ret, errMsg)
                Else
                    Commands.runCommand("SystemUtil.Run(""" & Commands.CustomEVal(sObject) & """)", ret, errMsg)
                End If

            Case "selectwindow", "select window"
                If IsNumeric(sData) Then
                    'psParentRootObject = psRootObject
                    psRootObject = "Browser(""index:=" & sData & """)"
                Else
                    'psParentRootObject = psRootObject
                    psRootObject = "Browser(""title:=" & sData & """)"
                End If

            Case "closewindow", "close window"
                If sData = "" Then
                    If psRootObject <> "" Then
                        Commands.runCommand(psRootObject & ".Close", ret, errMsg)
                        If errMsg = "SUCCESS" Then
                            CommandExecutor = "true"
                        Else
                            CommandExecutor = "false"
                            Throw (New Exception(psRootObject & ".Close(); ERROR: Object not found in application: " & errMsg))
                        End If
                        'psRootObject = psParentRootObject
                        psRootObject = ""
                    Else
                        CommandExecutor = "False"
                        Throw (New Exception(psRootObject & ".Close(); ERROR: Object not found in application: " & errMsg))
                    End If
                ElseIf IsNumeric(sData) Then
                    Commands.runCommand("Browser(""index:=" & sData & """).Close", ret, errMsg)
                    If errMsg = "SUCCESS" Then
                        CommandExecutor = "true"
                    Else
                        CommandExecutor = "false"
                        Throw (New Exception("Browser(""index:=" & sData & """).Close(); ERROR: Object not found in application: " & errMsg))
                    End If
                    'If psParentRootObject <> "" Then psRootObject = psParentRootObject
                Else
                    Commands.runCommand("Browser(""title:=" & sData & """).Close", ret, errMsg)
                    If errMsg = "SUCCESS" Then
                        CommandExecutor = "true"
                    Else
                        CommandExecutor = "false"
                        Throw (New Exception("Browser(""title:=" & sData & """).Close(); ERROR: Object not found in application: " & errMsg))
                    End If
                    'If psParentRootObject <> "" Then psRootObject = psParentRootObject
                End If
            Case "lastactionobject", "lastactionobjectid", "lastobject", "lastobjectid"
                CommandExecutor = psLastObject
            Case "generatedynamicobjectid", "generate dynamic objectid"
                AddItemToRepository(sObject, sData)
            Case Else
                sObject = Replace(sObject, "~", "")
                On Error GoTo 0 'Need to check all cases where this would be reset here.
                'aoObject = TC_GetKeyword(sObject)
                aoObject = GetORObject(sObject)
                If UBound(aoObject) = -1 Then
                    'not an object
                    Dim nIndex
                    If InStr(sObject, ",") > 0 Then 'not an object
                        sTemp = Split(sObject, ",")
                        sObject = sTemp(0)
                        RowId = sTemp(1)
                        ColId = sTemp(2)
                        If UBound(sTemp) = 3 Then
                            nIndex = sTemp(3)
                        Else
                            nIndex = 0
                        End If
                        aoObject = Core.GetORObject(sObject)
                        oObject = aoObject(0)
                        Commands.runCommand(oObject & ".GetToProperty(""class name"")", ret, errMsg)
                        'object could be other than webtable
                        If sActionName = "sync" Or sActionName = "waitforpagetoload" Then
                            'MsgBox(Core.psRootObject)
                            If UBound(aoObject) = -1 Then 'this seems to be dead code, need to check -pradeep
                                Dim nTimeout : nTimeout = 60
                                Do
                                    System.Threading.Thread.Sleep(1000)
                                    Commands.runCommand("Browser(""index:=0"").Exist(1)", ret, errMsg)
                                    If UCase(ret) = "TRUE" And errMsg = "SUCCESS" Then Exit Do
                                    nTimeout = nTimeout - 1
                                Loop Until nTimeout = 0
                                If nTimeout = 0 Then
                                    Throw (New Exception("Unable to Sync/waitforpagetoload; Source xAFTQTPEngine.Core.CommandExecutor"))
                                Else
                                    If Commands.runCommand("Browser(""index:=0"").Sync", ret, errMsg) Then
                                        CommandExecutor = ret
                                    Else
                                        CommandExecutor = ret
                                        Throw (New Exception("Unable to Sync/waitforpagetoload; Source xAFTQTPEngine.Core.CommandExecutor"))
                                    End If
                                End If
                            Else
                                If Commands.runCommand(Core.psRootObject & ".Sync", ret, errMsg) Then
                                    CommandExecutor = ret
                                Else
                                    CommandExecutor = ret
                                    Throw (New Exception("Unable to Sync/waitforpagetoload; Source xAFTQTPEngine.Core.CommandExecutor"))
                                End If

                            End If
                        ElseIf sActionName = "deletecookie" Or sActionName = "deletecookies" Then
                            If Commands.runCommand("Browser(""index:=0"").DeleteCookies", ret, errMsg) Then
                                CommandExecutor = "True" 'This doesnt return any value
                            Else
                                CommandExecutor = "False"
                            End If

                            '$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$ Keyword developed on 15-Oct-2012 Start $$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$
                        ElseIf sActionName = "gettitle" Or sActionName = "get title" Then
                            If Commands.runCommand("Browser(""index:=0"").GetROProperty(""title"")", ret, errMsg) Then
                                CommandExecutor = ret
                            Else
                                CommandExecutor = ret
                            End If
                            '$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$Keyword developed on 15-Oct-2012 End $$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$
                        ElseIf sActionName = "close" Or sActionName = "close browser" Or sActionName = "closebrowser" Then
                            If Commands.runCommand(psRootObject & ".Close", ret, errMsg) Then
                                CommandExecutor = "True"
                            Else
                                CommandExecutor = "False"
                            End If
                        ElseIf sActionName = "verifytext" Then
                            Commands.runCommand(psCurrentPage & ".GetROProperty(""innertext"")", ret, errMsg) 'This retrieves only 1024 bytes of data; need to check if data is more than this size
                            If InStr(1, ret, sData, 1) > 0 Then
                                CommandExecutor = "True"
                            Else
                                CommandExecutor = "False"
                            End If
                        ElseIf sActionName = "wait" Then
                            If LCase(Trim(sData)) = "novalue" Then
                                sData = CDbl(QTP._DefaultObjectTimeout)
                            Else
                                sData = CDbl(sData)
                            End If
                            If Commands.runCommand("Wait(0," & sData & ")", ret, errMsg) Then
                                CommandExecutor = ret
                            Else
                                CommandExecutor = ret
                            End If
                            '++++++++++++++++++++++++++++++++++ This Action is developed on 18 Oct 2012 Start +++++++++++++++++++++++++++++++++++++++++++++++++++++++
                        ElseIf sActionName = "waitfortext" Or sActionName = "waitfor text" Or sActionName = "wait for text" Then
                            Dim sPageText, btextflag
                            btextflag = False
                            If LCase(Trim(sData)) = "novalue" Or Trim(sData) = "" Then
                                sData = CInt(QTP._DefaultObjectTimeout) / 1000
                            Else
                                sData = CInt(sData) / 1000
                            End If
                            For i = 0 To sData
                                Commands.runCommand(psCurrentPage & ".Object.documentElement.innerText", ret, errMsg)
                                sPageText = ret
                                If InStr(1, sPageText, sObject, 1) > 0 Then
                                    btextflag = True
                                    Exit For
                                End If
                                Commands.runCommand("Wait(1)", ret, errMsg)
                            Next
                            If btextflag Then
                                CommandExecutor = "true"
                            Else
                                CommandExecutor = "false"
                            End If

                        ElseIf sActionName = "verifytextnotpresent" Or sActionName = "verify textnotpresent" Or sActionName = "verify text notpresent" Then
                            If LCase(Right(sObject, 4)) = ".pdf" Then
                                '<< Implementaion is not yet done >>
                            Else
                                Commands.runCommand(psCurrentPage & ".Object.documentElement.innerText", ret, errMsg)
                                If InStr(LCase(sData), LCase(ret)) = 0 Then
                                    CommandExecutor = "true"
                                Else
                                    CommandExecutor = "false"
                                End If
                            End If
                            '+++++++++++++++++++++++++++++++++++++++ This Action is developed on 18 Oct 2012 End ++++++++++++++++++++++++++++
                        ElseIf sActionName = "iselementpresent" Then
                            Throw (New Exception("Object not found / Unknown Action: OBJECT:" & sObject & " ACTION:" & sActionName & "; SOURCE: xAFTQTPEngine.Core.CommandExecutor"))

                        Else
                            'may be user defined method; Try run it, throw exception if it fails
                            'Commands.runCommand("Print ""Action: " & sActionName & "- Object not found! " & sObject & """", ret, errMsg)

                            If Len(sData) > 0 Then
                                If Commands.runCommand(sActionName + "(""" + sData + """)", ret, errMsg) Then '
                                    CommandExecutor = ret
                                Else
                                    Throw (New Exception("Object not found / Unknown Action: OBJECT:" & sObject & " ACTION:" & sActionName & "; SOURCE: xAFTQTPEngine.Core.CommandExecutor"))
                                End If
                            ElseIf Commands.runCommand(sActionName + "()", ret, errMsg) Then '
                                CommandExecutor = ret
                            Else
                                Throw (New Exception("Object not found / Unknown Action: OBJECT:" & sObject & " ACTION:" & sActionName & "; SOURCE: xAFTQTPEngine.Core.CommandExecutor"))
                            End If

                        End If
                    End If
                Else 'When object is present 
                    oObject = aoObject(0)
                    'MsgBox("finding from xml : " + oObject)
                    If UBound(aoObject) > 0 Then Throw (New Exception("Duplicate object found: " + Join(aoObject, ";")))
                    'For Each oObject In aoObject 'Looping through all the objects found; This case will not occur as we have a fix in place to raise exception on finding duplicates
                    'Commands.runCommand(oObject & ".Exist(1)", ret, errMsg) 'ret value will be either "True"/"False"
                    'If ret = "True" Then
                    'If QTP._HighlightObjects = "yes" Then Commands.runCommand(oObject & ".Highlight", ret, errMsg)

                    qtpObject = ""
                    sCommand = ""
                    qtpObject = Right(oObject, Len(oObject) - InStrRev(oObject, "."))
                    qtpObject = Mid(qtpObject, InStr(qtpObject, "(") + 1)
                    qtpObject = Trim(Left(qtpObject, InStr(qtpObject, ",") - 1))
                    qtpObject = Mid(qtpObject, 2, Len(qtpObject) - 2)
                    'MsgBox("object name: " & qtpObject)
                    sCommand = CommonConfig.getActionCommand(xAFTObject, qtpObject, sAction, sData)

                    If Left(sAction, 3) = "get" Then
                        CommandExecutor = Commands.CustomEVal(sData & "=""" & Commands.CustomEVal(oObject & ".GetRoProperty(" & Mid(sAction, 4) & ")") & """")
                    ElseIf sCommand = "" Then
                        'Unknown command
                        Throw (New Exception(
                               "Unknown Object: Couldnt find xAFTObject-TestCompleteObject mapping for the following object type and action: " & _
                                "TestCompleteObject- " + qtpObject & " " & _
                                "xAFTObject- " + xAFTObject & " " & _
                                "xAFTAction- " + sAction & " "
                                ))
                    Else
                        If Commands.runCommand(oObject & "." & sCommand, ret, errMsg) Then '
                            If errMsg = "SUCCESS" Then
                                CommandExecutor = ret
                            Else
                                CommandExecutor = ret
                                Throw (New Exception(oObject & "." & sCommand & "); ERROR:" & errMsg))
                            End If
                        Else
                            CommandExecutor = ret
                            Throw (New Exception("ERROR: " & sCommand & "action is not performed on object:" & oObject & "Error:" & errMsg))
                        End If
                    End If


                    If errMsg <> "" And errMsg <> "SUCCESS" Then
                        MsgBox("MISSING ERRORS:: Object: " & sObject & "Action: " & sActionName & " -Error: " & errMsg) 'Temp msg to see if there are any missed commands/exceptions
                        'Throw (New Exception("Object: " & sObject & "Action: " & sActionName & " -Error: " & errMsg)) 'raise any missed exceptions
                    End If
                    ''*****                End If ''  WebTable Condition if Statement end
                    'End If
                    psLastObject = oObject 'holding this in public var so tht nxt action cmd can use when needed
                    'Next
                End If

        End Select
    End Function

    Shared Function AppCommandExecutor(ByVal xAFTObject, ByVal sAction, ByVal sObject, ByVal sData)
        'MsgBox("AppStepExecutor: " & sAction & ":" & sObject)
        Dim sActionName, aoObject, oObject, arrColumNames, columindexFlag, arrAllDropdownItems, arrVerifyItemsInDropDown, nItemCountFlag
        Dim ret, errMsg, sTemp, RowId, ColId, arrItemsToSelect ', psParentRootObject
        Dim qtpObject, sCommand As String

        AppCommandExecutor = False
        sActionName = LCase(Trim(Replace(sAction, "~", "")))
        If UCase(Trim(Left(sAction, 7))) = "EXTLIB:" Then
            'This is qtp external method call
            sAction = Trim(Mid(sAction, 8))
            'Call this method;
        End If
        ret = ""
        errMsg = ""
        'MsgBox("AppCommandExecutor")
        Select Case sActionName
            Case "open"
                sObject = LCase(Trim(sObject))
                If sObject = "" Or sObject = "novalue" Then sObject = QTP._browserType 'nothing passed? use global browser var set at initialize method
                Select Case sObject
                    Case "firefox", "*firefox", "ff", "*ff"
                        If psRootObject <> "" Then
                            Commands.runCommand("Browser(""index:=0"").Navigate(""" & Commands.CustomEVal(sData) & """)", ret, errMsg)
                        Else
                            Commands.runCommand("SystemUtil.Run(""firefox"", """ & Commands.CustomEVal(sData) & """)", ret, errMsg)
                        End If
                    Case "*chrome", "chrome", "*gc", "gc", "googlechrome", "*googlechrome", "google chrome", "*google chrome"
                        If psRootObject <> "" Then
                            Commands.runCommand("Browser(""index:=0"").Navigate(""" & Commands.CustomEVal(sData) & """)", ret, errMsg)
                        Else
                            Commands.runCommand("SystemUtil.Run(""chrome"", """ & Commands.CustomEVal(sData) & """)", ret, errMsg)
                        End If
                    Case Else
                        If psRootObject <> "" Then
                            Commands.runCommand("Browser(""index:=0"").Navigate(""" & Commands.CustomEVal(sData) & """)", ret, errMsg)
                        Else
                            Commands.runCommand("SystemUtil.Run(""iexplore"", """ & Commands.CustomEVal(sData) & """)", ret, errMsg)
                        End If
                End Select
                AppCommandExecutor = ret
            Case "openapp", "openapplication", "open application", "open app"
                Commands.runCommand("SystemUtil.Run(""" & Commands.CustomEVal(sData) & """)", ret, errMsg)
            Case "methodcall" ' When u call some User defined functions
                'MsgBox("methodCall- " + sData)
                Dim sDataArray, buildDataParams
                Dim sDataType
                If Trim(sData) = "" Then
                    Commands.runCommand(sObject + "()", ret, errMsg)
                Else

                    sDataArray = Split(sData, ",")
                    For i = 0 To UBound(sDataArray)
                        sDataType = "String"
                        If InStr(sDataArray(i), "::") > 0 Then
                            sDataType = LCase(Trim(Left(sDataArray(i), InStr(sDataArray(i), "::") - 1)))
                            If sDataType = "int" Or sDataType = "float" Then
                                sDataType = "notString"
                            End If
                        End If
                        If sDataType = "notString" Then
                            sDataArray(i) = Trim(sDataArray(i))
                        Else
                            sDataArray(i) = """" + Trim(sDataArray(i)) + """"
                        End If
                    Next
                    Commands.runCommand(sObject + "(" + Join(sDataArray, ",") + ")", ret, errMsg)
                End If

                AppCommandExecutor = ret
            Case "openwindow", "open window"
                If QTP._browserType = "firefox" Or QTP._browserType = "*firefox" Or QTP._browserType = "ff" Or QTP._browserType = "*ff" Then
                    Commands.runCommand("SystemUtil.Run(""firefox" & """,""" & Commands.CustomEVal(sObject) & """)", ret, errMsg)
                Else
                    Commands.runCommand("SystemUtil.Run(""" & Commands.CustomEVal(sObject) & """)", ret, errMsg)
                End If

            Case "selectwindow", "select window"
                If IsNumeric(sData) Then
                    'psParentRootObject = psRootObject
                    psRootObject = "Browser(""index:=" & sData & """)"
                Else
                    'psParentRootObject = psRootObject
                    psRootObject = "Browser(""title:=" & sData & """)"
                End If

            Case "closewindow", "close window"
                ''''''''''''''''''' Old Defination ''''''''''''''''''''''''''''''''
                'If IsNumeric(sData) Then
                '    ' ''psRootObject = " rootobject"
                '    ' ''psParentRootObject = psRootObject
                '    ' ''psRootObject = "browser(""index:=" & sData & """)"
                '    ' ''MsgBox(psRootObject)
                '    If Commands.runCommand(psRootObject & ".Close", ret, errMsg) Then
                '        AppCommandExecutor = "True"
                '    Else
                '        AppCommandExecutor = "False"
                '    End If
                '    psRootObject = psParentRootObject
                'Else
                '    'MsgBox("It is not a numerice value")
                '    'psRootObject = " RootObject"
                '    'psParentRootObject = psRootObject
                '    'psRootObject = "Browser(""title:=" & sData & """)"
                '    'MsgBox(psRootObject)
                '    If Commands.runCommand(psRootObject & ".Close", ret, errMsg) Then
                '        AppCommandExecutor = "True"
                '    Else
                '        AppCommandExecutor = "False"
                '    End If
                '    psRootObject = psParentRootObject
                'End If
                '''''''''''''''''''' Old Defination End ''''''''''''''''''''
                ''''''''''''''''''' Old Defination ''''''''''''''''''''''''''''''''
                If sData = "" Then
                    If psRootObject <> "" Then
                        Commands.runCommand(psRootObject & ".Close", ret, errMsg)
                        If errMsg = "SUCCESS" Then
                            AppCommandExecutor = "true"
                        Else
                            AppCommandExecutor = "false"
                            Throw (New Exception(psRootObject & ".Close(); ERROR: Object not found in application: " & errMsg))
                        End If
                        'psRootObject = psParentRootObject
                        psRootObject = ""
                    Else
                        AppCommandExecutor = "False"
                        Throw (New Exception(psRootObject & ".Close(); ERROR: Object not found in application: " & errMsg))
                    End If
                ElseIf IsNumeric(sData) Then
                    Commands.runCommand("Browser(""index:=" & sData & """).Close", ret, errMsg)
                    If errMsg = "SUCCESS" Then
                        AppCommandExecutor = "true"
                    Else
                        AppCommandExecutor = "false"
                        Throw (New Exception("Browser(""index:=" & sData & """).Close(); ERROR: Object not found in application: " & errMsg))
                    End If
                    'If psParentRootObject <> "" Then psRootObject = psParentRootObject
                Else
                    Commands.runCommand("Browser(""title:=" & sData & """).Close", ret, errMsg)
                    If errMsg = "SUCCESS" Then
                        AppCommandExecutor = "true"
                    Else
                        AppCommandExecutor = "false"
                        Throw (New Exception("Browser(""title:=" & sData & """).Close(); ERROR: Object not found in application: " & errMsg))
                    End If
                    'If psParentRootObject <> "" Then psRootObject = psParentRootObject
                End If
            Case "lastactionobject", "lastactionobjectid", "lastobject", "lastobjectid"
                AppCommandExecutor = psLastObject
            Case "generatedynamicobjectid", "generate dynamic objectid"
                AddItemToRepository(sObject, sData)
            Case Else
                sObject = Replace(sObject, "~", "")
                On Error GoTo 0 'Need to check all cases where this would be reset here.
                aoObject = Core.GetORObject(sObject)
                If UBound(aoObject) = -1 Then
                    'not an object
                    Dim nIndex
                    If InStr(sObject, ",") > 0 Then 'not an object
                        sTemp = Split(sObject, ",")
                        sObject = sTemp(0)
                        RowId = sTemp(1)
                        ColId = sTemp(2)
                        If UBound(sTemp) = 3 Then
                            nIndex = sTemp(3)
                        Else
                            nIndex = 0
                        End If
                        aoObject = Core.GetORObject(sObject)
                        oObject = aoObject(0)
                        Commands.runCommand(oObject & ".GetToProperty(""class name"")", ret, errMsg)
                        If LCase(Trim(ret)) = "webtable" Then
                            Select Case LCase(Trim(Replace(sAction, "~", "")))
                                Case "type"
                                    'MsgBox("Type Action")
                                    '    oObject = oObject & ".ChildItem(" & RowId & "," & ColId & ",""WebEdit"",0).Set(""" & sData & """)"

                                    oObject = oObject & ".ChildItem(" & RowId & "," & ColId & ",""WebEdit""," & nIndex & ").Set(""" & sData & """)"
                                    'MsgBox(oObject)
                                    Commands.runCommand(oObject, ret, errMsg)
                                Case "click"
                                    Dim aWebObjectTypes = {"WebEdit", "WebList", "Link", "WebButton", "WebCheckbox", "WebRadioGroup", "WebElement"}
                                    Dim sObjectType, sTempObject
                                    sTempObject = oObject
                                    For Each sWebType In aWebObjectTypes
                                        oObject = oObject & ".ChildItemCount(" & RowId & "," & ColId & ",""" & sWebType & """)"
                                        Commands.runCommand(oObject, ret, errMsg)
                                        If ret > 0 Then
                                            sObjectType = sWebType
                                            Exit For
                                        End If
                                        oObject = sTempObject
                                    Next
                                    oObject = sTempObject
                                    oObject = oObject & ".ChildItem(" & RowId & "," & ColId & ",""" & sObjectType & """," & nIndex & ").click"
                                    Commands.runCommand(oObject, ret, errMsg)
                                Case "select", "selectoption", "select option"
                                    oObject = oObject & ".ChildItem(" & RowId & "," & ColId & ",""WebList""," & nIndex & ").Select(""" & sData & """)"
                                    Commands.runCommand(oObject, ret, errMsg)
                                Case "check"
                                    oObject = oObject & ".ChildItem(" & RowId & "," & ColId & ",""WebCheckbox""," & nIndex & ").Set(""on"")"
                                    'MsgBox(oObject)
                                    Commands.runCommand(oObject, ret, errMsg)
                                Case "uncheck"
                                    oObject = oObject & ".ChildItem(" & RowId & "," & ColId & ",""WebCheckbox""," & nIndex & ").Set(""off"")"
                                    'MsgBox(oObject)
                                    Commands.runCommand(oObject, ret, errMsg)
                                Case "verifyselectedvalue", "verify selectedvalue"
                                    'oObject = oObject & ".ChildItem(" & RowId & "," & ColId & ",""WebCheckbox""," & nIndex & ").Set(""off"")"
                                    ' MsgBox(oObject)
                                    'Commands.runCommand(oObject, ret, errMsg)
                                Case "verifytext", "verify text"
                                    oObject = oObject & ".ChildItem(" & RowId & "," & ColId & ",""WebElement""," & nIndex & ")"
                                    If Commands.runCommand(oObject & ".Exist(1)", ret, errMsg) Then
                                        Commands.runCommand(oObject & ".GetROProperty(""innertext"")", ret, errMsg)
                                        If InStr(LCase(Trim(sData)), LCase(Trim(ret))) > 0 Then
                                            AppCommandExecutor = "true"
                                        Else
                                            AppCommandExecutor = "false"
                                        End If
                                    Else
                                        AppCommandExecutor = "false"
                                    End If
                                Case "verifyvalue", "verify value"
                                    Dim aWebObjectTypes = {"WebEdit", "WebList", "WebCheckbox"}
                                    Dim sObjectType, sTempObject
                                    sTempObject = oObject
                                    For Each sWebType In aWebObjectTypes
                                        oObject = oObject & ".ChildItemCount(" & RowId & "," & ColId & ",""" & sWebType & """)"
                                        Commands.runCommand(oObject, ret, errMsg)
                                        If ret > 0 Then
                                            sObjectType = sWebType
                                            Exit For
                                        End If
                                        oObject = sTempObject
                                    Next
                                    oObject = sTempObject
                                    oObject = oObject & ".ChildItem(" & RowId & "," & ColId & ",""" & sObjectType & """," & nIndex & ")"
                                    'oObject = oObject & ".ChildItem(" & RowId & "," & ColId & ",""WebEdit""," & nIndex & ")"
                                    If Commands.runCommand(oObject & ".Exist(1)", ret, errMsg) Then
                                        sData = Commands.CustomEVal(sData)
                                        Commands.runCommand(oObject & ".GetROProperty(""value"")", ret, errMsg)
                                        If LCase(Trim(ret)) = LCase(Trim(sData)) Then
                                            AppCommandExecutor = "true"
                                        Else
                                            AppCommandExecutor = "false"
                                        End If
                                    Else
                                        AppCommandExecutor = ret
                                        Throw (New Exception(oObject & ".Exist(1); ERROR: Object not found in application: " & errMsg))
                                    End If
                                Case "verifyselectoptions", "verify select options", "verify selectoptions"
                                    oObject = oObject & ".ChildItem(" & RowId & "," & ColId & ",""WebList""," & nIndex & ")"
                                    'Commands.runCommand(oObject, ret, errMsg)
                                    If Commands.runCommand(oObject & ".Exist(1)", ret, errMsg) Then
                                        sData = Commands.CustomEVal(sData)
                                        Commands.runCommand(oObject & ".GetROProperty(""all items"")", ret, errMsg)
                                        arrAllDropdownItems = Split(ret, ";", -1, 1)
                                        arrVerifyItemsInDropDown = Split(sData, ",", -1, 1)
                                        nItemCountFlag = 0
                                        For iVerifyItemIndex = 0 To UBound(arrVerifyItemsInDropDown)
                                            For iItemIndex = 0 To UBound(arrAllDropdownItems)
                                                If LCase(Trim(arrVerifyItemsInDropDown(iVerifyItemIndex))) = LCase(Trim(arrAllDropdownItems(iItemIndex))) Then
                                                    nItemCountFlag = nItemCountFlag + 1
                                                    Exit For
                                                End If
                                            Next
                                        Next
                                        If nItemCountFlag = UBound(arrVerifyItemsInDropDown) + 1 Then
                                            If errMsg = "SUCCESS" Then
                                                AppCommandExecutor = "true"
                                            Else
                                                AppCommandExecutor = "false"
                                                Throw (New Exception("verifyselectoptions : " & sData & " on Combobox or Drop Down object:" & oObject & " ; ERROR:" & errMsg))
                                            End If
                                        Else
                                            AppCommandExecutor = ret
                                            Throw (New Exception("ERROR: verifyselectoptions:" & sData & "on Combobox or Drop Down object:" & oObject & " is failed ErrorMsg:" & errMsg))
                                        End If
                                    Else
                                        AppCommandExecutor = ret
                                        Throw (New Exception(oObject & ".Exist(1); ERROR: Object not found in application: " & errMsg))
                                    End If
                                Case "verifystate", "verify state"
                                    Dim aWebObjectTypes = {"WebEdit", "WebList", "Link", "WebCheckbox", "WebRadioGroup"}
                                    Dim sObjectType, sTempObject
                                    sTempObject = oObject
                                    For Each sWebType In aWebObjectTypes
                                        oObject = oObject & ".ChildItemCount(" & RowId & "," & ColId & ",""" & sWebType & """)"
                                        Commands.runCommand(oObject, ret, errMsg)
                                        If ret > 0 Then
                                            sObjectType = sWebType
                                            Exit For
                                        End If
                                        oObject = sTempObject
                                    Next
                                    oObject = sTempObject
                                    oObject = oObject & ".ChildItem(" & RowId & "," & ColId & ",""" & sObjectType & """," & nIndex & ")"
                                    If Commands.runCommand(oObject & ".Exist(1)", ret, errMsg) Then
                                        sData = Commands.CustomEVal(sData)
                                        If LCase(Trim(sData)) = "visible" Then
                                            Commands.runCommand(oObject & ".GetROProperty(""visible"")", ret, errMsg)
                                            If LCase(Trim(ret)) = True Then
                                                AppCommandExecutor = "true"
                                            Else
                                                AppCommandExecutor = "false"
                                            End If
                                        ElseIf LCase(Trim(sData)) = "enabled" Then
                                            Commands.runCommand(oObject & ".GetROProperty(""disabled"")", ret, errMsg)
                                            If LCase(Trim(ret)) = 0 Then
                                                AppCommandExecutor = "true"
                                            Else
                                                AppCommandExecutor = "false"
                                            End If
                                        ElseIf LCase(Trim(sData)) = "disabled" Then
                                            Commands.runCommand(oObject & ".GetROProperty(""disabled"")", ret, errMsg)
                                            If LCase(Trim(ret)) = 1 Then
                                                AppCommandExecutor = "true"
                                            Else
                                                AppCommandExecutor = "false"
                                            End If
                                        End If
                                    Else
                                        AppCommandExecutor = ret
                                        Throw (New Exception(oObject & ".Exist(1); ERROR: Object not found in application: " & errMsg))
                                    End If
                                Case "getvalue", "get value"
                                    Dim aWebObjectTypes = {"WebEdit", "WebList", "WebCheckbox"}
                                    Dim sObjectType, sTempObject
                                    sTempObject = oObject
                                    For Each sWebType In aWebObjectTypes
                                        oObject = oObject & ".ChildItemCount(" & RowId & "," & ColId & ",""" & sWebType & """)"
                                        Commands.runCommand(oObject, ret, errMsg)
                                        If ret > 0 Then
                                            sObjectType = sWebType
                                            Exit For
                                        End If
                                        oObject = sTempObject
                                    Next
                                    oObject = sTempObject
                                    oObject = oObject & ".ChildItem(" & RowId & "," & ColId & ",""" & sObjectType & """," & nIndex & ")"
                                    If Commands.runCommand(oObject & ".Exist(1)", ret, errMsg) Then
                                        If LCase(Trim(sObjectType)) = "webcheckbox" Then
                                            If Commands.runCommand(oObject & ".GetROProperty(""checked"")", ret, errMsg) Then
                                                If errMsg = "SUCCESS" Then
                                                    If ret = "0" Then
                                                        AppCommandExecutor = "off"
                                                    Else
                                                        AppCommandExecutor = "on"
                                                    End If
                                                Else
                                                    AppCommandExecutor = ret
                                                    Throw (New Exception(oObject & ".GetROProperty(""value""); ERROR:" & errMsg))
                                                End If
                                            Else
                                                AppCommandExecutor = ret
                                                Throw (New Exception("ERROR: getValue action is not performed on object:" & oObject & "Error:" & errMsg))
                                            End If
                                        Else
                                            If Commands.runCommand(oObject & ".GetROProperty(""value"")", ret, errMsg) Then
                                                If errMsg = "SUCCESS" Then
                                                    AppCommandExecutor = ret
                                                Else
                                                    AppCommandExecutor = ret
                                                    Throw (New Exception(oObject & ".GetROProperty(""value""); ERROR:" & errMsg))
                                                End If
                                            Else
                                                AppCommandExecutor = ret
                                                Throw (New Exception("ERROR: getValue action is not performed on object:" & oObject & "Error:" & errMsg))
                                            End If
                                        End If
                                    Else
                                        AppCommandExecutor = ret
                                        Throw (New Exception(oObject & ".Exist(1); ERROR: Object not found in application: " & errMsg))
                                    End If
                                Case "gettext"
                                    Dim aWebObjectTypes = {"WebElement", "Link"}
                                    Dim sObjectType, sAttribute, sTempObject
                                    sTempObject = oObject
                                    For Each sWebType In aWebObjectTypes
                                        oObject = oObject & ".ChildItemCount(" & RowId & "," & ColId & ",""" & sWebType & """)"
                                        Commands.runCommand(oObject, ret, errMsg)
                                        If ret > 0 Then
                                            sObjectType = sWebType
                                            Exit For
                                        End If
                                        oObject = sTempObject
                                    Next
                                    oObject = sTempObject
                                    If LCase(Trim(sObjectType)) = "webelement" Then
                                        sAttribute = "outertext"
                                    Else
                                        sAttribute = "text"
                                    End If
                                    oObject = oObject & ".ChildItem(" & RowId & "," & ColId & ",""" & sObjectType & """," & nIndex & ")"
                                    'oObject = oObject & ".ChildItem(" & RowId & "," & ColId & ",""WebElement""," & nIndex & ")"
                                    If Commands.runCommand(oObject & ".Exist(1)", ret, errMsg) Then
                                        'If Commands.runCommand(oObject & ".GetROProperty(""outertext"")", ret, errMsg) Then
                                        '    If errMsg = "SUCCESS" Then
                                        '        If ret = "" Then Commands.runCommand(oObject & ".GetRoProperty(""text"")", ret, errMsg) 'for some we get text from text attribute
                                        '        AppCommandExecutor = ret
                                        '    Else
                                        '        AppCommandExecutor = ret
                                        '        Throw (New Exception(oObject & ".GetROProperty(""outertext""); ERROR:" & errMsg))
                                        '    End If
                                        'Else
                                        '    AppCommandExecutor = ret
                                        '    Throw (New Exception("ERROR: getText action is not performed on object:" & oObject & "Error:" & errMsg))
                                        'End If
                                        If Commands.runCommand(oObject & ".GetROProperty(""" & sAttribute & """)", ret, errMsg) Then
                                            If errMsg = "SUCCESS" Then
                                                AppCommandExecutor = ret
                                            Else
                                                AppCommandExecutor = ret
                                                Throw (New Exception(oObject & ".GetROProperty(""" & sAttribute & """); ERROR:" & errMsg))
                                            End If
                                        Else
                                            AppCommandExecutor = ret
                                            Throw (New Exception("ERROR: getText action is not performed on object:" & oObject & "Error:" & errMsg))
                                        End If

                                    Else
                                        AppCommandExecutor = ret
                                        Throw (New Exception(oObject & ".Exist(1); ERROR: Object not found in application: " & errMsg))
                                    End If
                                    'Case "waitforelementpresent"
                                Case "selectoptionbyvalue"
                                    oObject = oObject & ".ChildItem(" & RowId & "," & ColId & ",""WebList""," & nIndex & ").SelectOptionByValue(""" & sData & """)"
                                    Commands.runCommand(oObject, ret, errMsg)
                                    If errMsg = "SUCCESS" Then
                                        AppCommandExecutor = ret
                                    Else
                                        AppCommandExecutor = ret
                                        Throw (New Exception(oObject & ".SelectOptionByValue(""" & sData & """); ERROR:" & errMsg))
                                    End If
                                Case "selectoptionbylabel"
                                    oObject = oObject & ".ChildItem(" & RowId & "," & ColId & ",""WebList""," & nIndex & ").select(""" & sData & """)"
                                    Commands.runCommand(oObject, ret, errMsg)
                                    If errMsg = "SUCCESS" Then
                                        AppCommandExecutor = ret
                                    Else
                                        AppCommandExecutor = ret
                                        Throw (New Exception(oObject & ".selectoptionbylabel(""" & sData & """); ERROR:" & errMsg))
                                    End If
                                Case "selectoptionbyindex"
                                    sData = Commands.CustomEVal(sData - 1)
                                    oObject = oObject & ".ChildItem(" & RowId & "," & ColId & ",""WebList""," & nIndex & ").select(""#" & sData & """)"
                                    Commands.runCommand(oObject, ret, errMsg)
                                    If errMsg = "SUCCESS" Then
                                        AppCommandExecutor = ret
                                    Else
                                        AppCommandExecutor = ret
                                        Throw (New Exception(oObject & ".selectoptionbylabel(""" & sData & """); ERROR:" & errMsg))
                                    End If
                                Case "getcolumnname"
                                    If Commands.runCommand(oObject & ".Exist(1)", ret, errMsg) Then
                                        If Commands.runCommand(oObject & ".GetROProperty(""rows"")", ret, errMsg) Then
                                            If errMsg = "SUCCESS" Then
                                                AppCommandExecutor = ret
                                            Else
                                                AppCommandExecutor = ret
                                                Throw (New Exception(oObject & ".GetROProperty(""rows""); ERROR:" & errMsg))
                                            End If
                                        Else
                                            AppCommandExecutor = ret
                                            Throw (New Exception("ERROR: getTableRowCount action is not performed on object:" & oObject & "Error:" & errMsg))
                                        End If
                                    Else
                                        AppCommandExecutor = ret
                                        Throw (New Exception(oObject & ".Exist(1); ERROR: Object not found in application: " & errMsg))
                                    End If
                                Case "gettablerowcount"
                                    If Commands.runCommand(oObject & ".Exist(1)", ret, errMsg) Then
                                        If Commands.runCommand(oObject & ".GetROProperty(""rows"")", ret, errMsg) Then
                                            If errMsg = "SUCCESS" Then
                                                AppCommandExecutor = ret
                                            Else
                                                AppCommandExecutor = ret
                                                Throw (New Exception(oObject & ".GetROProperty(""rows""); ERROR:" & errMsg))
                                            End If
                                        Else
                                            AppCommandExecutor = ret
                                            Throw (New Exception("ERROR: getTableRowCount action is not performed on object:" & oObject & "Error:" & errMsg))
                                        End If
                                    Else
                                        AppCommandExecutor = ret
                                        Throw (New Exception(oObject & ".Exist(1); ERROR: Object not found in application: " & errMsg))
                                    End If
                                Case "gettablecolumncount"
                                    If Commands.runCommand(oObject & ".Exist(1)", ret, errMsg) Then
                                        If Commands.runCommand(oObject & ".GetROProperty(""cols"")", ret, errMsg) Then
                                            If errMsg = "SUCCESS" Then
                                                AppCommandExecutor = ret
                                            Else
                                                AppCommandExecutor = ret
                                                Throw (New Exception(oObject & ".GetROProperty(""cols"") ERROR: " & errMsg))
                                            End If
                                        Else
                                            AppCommandExecutor = ret
                                            Throw (New Exception("ERROR: getTableColumnCount action is not performed on object:" & oObject & "Error:" & errMsg))
                                        End If
                                    Else
                                        AppCommandExecutor = ret
                                        Throw (New Exception(oObject & ".Exist(1); ERROR: Object not found in application: " & errMsg))
                                    End If
                                Case "getrowid"
                                    If InStr(sData, ",") > 0 Then
                                        sTemp = Split(sData, ",", -1, 1)
                                        sData = sTemp(0)
                                    End If
                                    If Commands.runCommand(oObject & ".Exist(1)", ret, errMsg) Then
                                        sData = Commands.CustomEVal(sData)
                                        If Commands.runCommand(oObject & ".GetRowWithCellText(""" & sData & """)", ret, errMsg) Then
                                            If errMsg = "SUCCESS" Then
                                                AppCommandExecutor = ret
                                            Else
                                                AppCommandExecutor = ret
                                                Throw (New Exception(oObject & ".GetRowWithCellText(""" & sData & """); ERROR:" & errMsg))
                                            End If
                                        Else
                                            AppCommandExecutor = ret
                                            Throw (New Exception("ERROR: getRowIndex action is not performed on object:" & oObject & "Error:" & errMsg))
                                        End If
                                    Else
                                        AppCommandExecutor = ret
                                        Throw (New Exception(oObject & ".Exist(1); ERROR: Object not found in application: " & errMsg))
                                    End If

                                Case "getcolumnid"
                                    If Commands.runCommand(oObject & ".Exist(1)", ret, errMsg) Then
                                        Commands.runCommand(oObject & ".GetROProperty(""column names"")", ret, errMsg)
                                        sData = Commands.CustomEVal(sData)
                                        arrColumNames = Split(ret, ";", -1, 1)
                                        For intLoopIndex = 0 To UBound(arrColumNames)
                                            If arrColumNames(intLoopIndex) = sData Then
                                                ret = intLoopIndex + 1
                                                columindexFlag = True
                                                Exit For
                                            End If
                                        Next intLoopIndex
                                        If columindexFlag Then
                                            If errMsg = "SUCCESS" Then
                                                AppCommandExecutor = ret
                                            Else
                                                AppCommandExecutor = ret
                                                Throw (New Exception("getcolumnindex of Column name: " & sData & " in WebTable object:" & oObject & " ; ERROR:" & errMsg))
                                            End If
                                        Else
                                            AppCommandExecutor = ret
                                            Throw (New Exception("ERROR: column name:" & sData & "is does not exist in table" & errMsg))
                                        End If
                                    Else
                                        AppCommandExecutor = ret
                                        Throw (New Exception(oObject & ".Exist(1); ERROR: Object not found in application: " & errMsg))
                                    End If
                            End Select
                        End If  'Webtable Check end
                        '                    End If  ' Check object has having morethan one parameter
                    Else 'object could be other than webtable
                        If sActionName = "sync" Or sActionName = "waitforpagetoload" Then
                            'MsgBox(Core.psRootObject)
                            If UBound(aoObject) = -1 Then 'this seems to be dead code, need to check -pradeep
                                Dim nTimeout : nTimeout = 60
                                Do
                                    System.Threading.Thread.Sleep(1000)
                                    Commands.runCommand("Browser(""index:=0"").Exist(1)", ret, errMsg)
                                    If UCase(ret) = "TRUE" And errMsg = "SUCCESS" Then Exit Do
                                    nTimeout = nTimeout - 1
                                Loop Until nTimeout = 0
                                If nTimeout = 0 Then
                                    Throw (New Exception("Unable to Sync/waitforpagetoload; Source xAFTQTPEngine.Core.AppCommandExecutor"))
                                Else
                                    If Commands.runCommand("Browser(""index:=0"").Sync", ret, errMsg) Then
                                        AppCommandExecutor = ret
                                    Else
                                        AppCommandExecutor = ret
                                        Throw (New Exception("Unable to Sync/waitforpagetoload; Source xAFTQTPEngine.Core.AppCommandExecutor"))
                                    End If
                                End If
                            Else
                                If Commands.runCommand(Core.psRootObject & ".Sync", ret, errMsg) Then
                                    AppCommandExecutor = ret
                                Else
                                    AppCommandExecutor = ret
                                    Throw (New Exception("Unable to Sync/waitforpagetoload; Source xAFTQTPEngine.Core.AppCommandExecutor"))
                                End If

                            End If
                        ElseIf sActionName = "deletecookie" Or sActionName = "deletecookies" Then
                            If Commands.runCommand("Browser(""index:=0"").DeleteCookies", ret, errMsg) Then
                                AppCommandExecutor = "True" 'This doesnt return any value
                            Else
                                AppCommandExecutor = "False"
                            End If

                            '$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$ Keyword developed on 15-Oct-2012 Start $$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$
                        ElseIf sActionName = "gettitle" Or sActionName = "get title" Then
                            If Commands.runCommand("Browser(""index:=0"").GetROProperty(""title"")", ret, errMsg) Then
                                AppCommandExecutor = ret
                            Else
                                AppCommandExecutor = ret
                            End If
                            '$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$Keyword developed on 15-Oct-2012 End $$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$
                        ElseIf sActionName = "close" Or sActionName = "close browser" Or sActionName = "closebrowser" Then
                            If Commands.runCommand(psRootObject & ".Close", ret, errMsg) Then
                                AppCommandExecutor = "True"
                            Else
                                AppCommandExecutor = "False"
                            End If
                        ElseIf sActionName = "verifytext" Then
                            Commands.runCommand(psCurrentPage & ".GetROProperty(""innertext"")", ret, errMsg) 'This retrieves only 1024 bytes of data; need to check if data is more than this size
                            If InStr(1, ret, sData, 1) > 0 Then
                                AppCommandExecutor = "True"
                            Else
                                AppCommandExecutor = "False"
                            End If
                        ElseIf sActionName = "wait" Then
                            If LCase(Trim(sData)) = "novalue" Then
                                sData = CDbl(QTP._DefaultObjectTimeout)
                            Else
                                sData = CDbl(sData)
                            End If
                            If Commands.runCommand("Wait(0," & sData & ")", ret, errMsg) Then
                                AppCommandExecutor = ret
                            Else
                                AppCommandExecutor = ret
                            End If
                            '++++++++++++++++++++++++++++++++++ This Action is developed on 18 Oct 2012 Start +++++++++++++++++++++++++++++++++++++++++++++++++++++++
                        ElseIf sActionName = "waitfortext" Or sActionName = "waitfor text" Or sActionName = "wait for text" Then
                            Dim sPageText, btextflag
                            btextflag = False
                            If LCase(Trim(sData)) = "novalue" Or Trim(sData) = "" Then
                                sData = CInt(QTP._DefaultObjectTimeout) / 1000
                            Else
                                sData = CInt(sData) / 1000
                            End If
                            For i = 0 To sData
                                Commands.runCommand(psCurrentPage & ".Object.documentElement.innerText", ret, errMsg)
                                sPageText = ret
                                If InStr(1, sPageText, sObject, 1) > 0 Then
                                    btextflag = True
                                    Exit For
                                End If
                                Commands.runCommand("Wait(1)", ret, errMsg)
                            Next
                            If btextflag Then
                                AppCommandExecutor = "true"
                            Else
                                AppCommandExecutor = "false"
                            End If

                        ElseIf sActionName = "verifytextnotpresent" Or sActionName = "verify textnotpresent" Or sActionName = "verify text notpresent" Then
                            If LCase(Right(sObject, 4)) = ".pdf" Then
                                '<< Implementaion is not yet done >>
                            Else
                                Commands.runCommand(psCurrentPage & ".Object.documentElement.innerText", ret, errMsg)
                                If InStr(LCase(sData), LCase(ret)) = 0 Then
                                    AppCommandExecutor = "true"
                                Else
                                    AppCommandExecutor = "false"
                                End If
                            End If
                            '+++++++++++++++++++++++++++++++++++++++ This Action is developed on 18 Oct 2012 End ++++++++++++++++++++++++++++
                        ElseIf sActionName = "iselementpresent" Then
                            Throw (New Exception("Object not found / Unknown Action: OBJECT:" & sObject & " ACTION:" & sActionName & "; SOURCE: xAFTQTPEngine.Core.AppCommandExecutor"))

                        Else
                            'may be user defined method; Try run it, throw exception if it fails
                            'Commands.runCommand("Print ""Action: " & sActionName & "- Object not found! " & sObject & """", ret, errMsg)

                            If Len(sData) > 0 Then
                                If Commands.runCommand(sActionName + "(""" + sData + """)", ret, errMsg) Then '
                                    AppCommandExecutor = ret
                                Else
                                    Throw (New Exception("Object not found / Unknown Action: OBJECT:" & sObject & " ACTION:" & sActionName & "; SOURCE: xAFTQTPEngine.Core.AppCommandExecutor"))
                                End If
                            ElseIf Commands.runCommand(sActionName + "()", ret, errMsg) Then '
                                AppCommandExecutor = ret
                            Else
                                Throw (New Exception("Object not found / Unknown Action: OBJECT:" & sObject & " ACTION:" & sActionName & "; SOURCE: xAFTQTPEngine.Core.AppCommandExecutor"))
                            End If

                        End If
                    End If
                Else 'When object is present 
                    oObject = aoObject(0)
                    If UBound(aoObject) > 0 Then Throw (New Exception("Duplicate object found: " + Join(aoObject, ";")))
                    'For Each oObject In aoObject 'Looping through all the objects found; This case will not occur as we have a fix in place to raise exception on finding duplicates
                    'Commands.runCommand(oObject & ".Exist(1)", ret, errMsg) 'ret value will be either "True"/"False"
                    'If ret = "True" Then
                    If QTP._HighlightObjects = "yes" Then Commands.runCommand(oObject & ".Highlight", ret, errMsg)

                    Select Case LCase(Trim(Replace(sAction, "~", "")))
                        Case "click"
                            If Commands.runCommand(oObject & ".Exist(1)", ret, errMsg) Then
                                If Commands.runCommand(oObject & ".Click()", ret, errMsg) Then
                                    If errMsg = "SUCCESS" Then
                                        AppCommandExecutor = ret
                                    Else
                                        AppCommandExecutor = ret
                                        Throw (New Exception(oObject & ".Click(); ERROR:" & errMsg))
                                    End If
                                Else
                                    AppCommandExecutor = ret
                                    Throw (New Exception("ERROR: Click action is not performed on object:" & oObject & "Error:" & errMsg))
                                End If
                            Else
                                AppCommandExecutor = ret
                                Throw (New Exception(oObject & ".Exist(1); ERROR: Object not found in application: " & errMsg))
                            End If
                        Case "sync", "waitforpagetoload", "waitforpageload"
                            If Core.psRootObject.Trim() = "" Then
                                Do
                                    System.Threading.Thread.Sleep(1000)
                                    If errMsg <> "" Then Exit Do
                                Loop Until Commands.runCommand("Browser(""index:=0"").Exist(1)", ret, errMsg)
                                If Commands.runCommand("Browser(""index:=0"").Sync", ret, errMsg) Then
                                    If errMsg = "SUCCESS" Then
                                        AppCommandExecutor = ret
                                    Else
                                        AppCommandExecutor = ret
                                        Throw (New Exception("Unable to Sync/waitforpagetoload; Source xAFTQTPEngine.Core.AppCommandExecutor: ERROR " & errMsg))
                                    End If
                                End If
                            Else
                                If Commands.runCommand(Core.psRootObject & ".Sync", ret, errMsg) Then
                                    If errMsg = "SUCCESS" Then
                                        AppCommandExecutor = ret
                                    Else
                                        AppCommandExecutor = ret
                                        Throw (New Exception("Unable to Sync/waitforpagetoload; Source xAFTQTPEngine.Core.AppCommandExecutor: ERROR " & errMsg))
                                    End If
                                Else
                                    AppCommandExecutor = ret
                                End If
                            End If
                        Case "type"
                            Dim ActionCommand
                            If InStr(1, Trim(Core.psRootObject), "Browser(", 1) = 1 Then
                                ActionCommand = "Set" 'For web based
                            Else
                                ActionCommand = "Type" 'for windows based
                            End If
                            If Commands.runCommand(oObject & ".Exist(1)", ret, errMsg) Then
                                sData = Commands.CustomEVal(sData)
                                If Commands.runCommand(oObject & "." & ActionCommand & "(""" & sData & """)", ret, errMsg) Then
                                    If errMsg = "SUCCESS" Then
                                        AppCommandExecutor = ret
                                    Else
                                        AppCommandExecutor = ret
                                        Throw (New Exception(oObject & "." & ActionCommand & (""" & sData & """) & "; ERROR:" & errMsg))
                                    End If
                                Else
                                    AppCommandExecutor = ret
                                    Throw (New Exception("ERROR: type action is not performed on object:" & oObject & "Error:" & errMsg))
                                End If
                            Else
                                AppCommandExecutor = ret
                                Throw (New Exception(oObject & ".Exist(1); ERROR: Object not found in application: " & errMsg))
                            End If
                        Case "waitforelement", "waitforelementpresent"
                            If LCase(Trim(sData)) = "novalue" Or Trim(sData) = "" Then
                                sData = CDbl(QTP._DefaultObjectTimeout) / 1000
                            Else
                                sData = CDbl(sData) / 1000
                            End If
                            If Commands.runCommand(oObject & ".Exist(" & sData & ")", ret, errMsg) Then
                                If errMsg = "SUCCESS" Then
                                    AppCommandExecutor = ret
                                Else
                                    AppCommandExecutor = ret
                                    Throw (New Exception(oObject & ".Set(""" & sData & """); ERROR:" & errMsg))
                                End If
                            Else
                                AppCommandExecutor = ret
                                Throw (New Exception("ERROR: Wait For Element operation failed:" & oObject & "Error:" & " Timeout occurred!"))
                            End If
                        Case "waitfortext", "waitfor text", "wait for text"
                            Dim sPageText, btextflag, nCounter
                            btextflag = False
                            nCounter = CInt(QTP._DefaultObjectTimeout) / 1000
                            If Commands.runCommand(oObject & ".Exist(" & nCounter & ")", ret, errMsg) Then
                                If errMsg = "SUCCESS" Then
                                    AppCommandExecutor = ret
                                Else
                                    AppCommandExecutor = ret
                                    Throw (New Exception(oObject & ".Set(""" & nCounter & """); ERROR:" & errMsg))
                                End If
                            Else
                                AppCommandExecutor = ret
                                Throw (New Exception("ERROR: Wait For Element operation failed:" & oObject & "Error:" & " Timeout occurred!"))
                            End If

                            For i = 0 To nCounter
                                On Error Resume Next
                                Commands.runCommand(oObject & ".GetRoProperty(""innerText"")", ret, errMsg)

                                sPageText = ret
                                If InStr(1, sPageText, sData, 1) > 0 And Err.Number = 0 Then
                                    btextflag = True
                                    Exit For
                                End If
                                On Error GoTo 0
                                Commands.runCommand("Wait(1)", ret, errMsg)
                            Next
                            If btextflag Then
                                AppCommandExecutor = "true"
                            Else
                                AppCommandExecutor = "false"
                            End If

                        Case "iselementpresent"
                            If Commands.runCommand(oObject & ".Exist(" & CInt(QTP._DefaultObjectTimeout) / 1000 & ")", ret, errMsg) Then 'need to use this wait time from aft config
                                If errMsg = "SUCCESS" Then
                                    AppCommandExecutor = LCase(Trim(ret))
                                Else
                                    Throw (New Exception("ERROR: isElementPresent operation failed:" & oObject & "Error:" & errMsg))
                                End If
                            Else
                                AppCommandExecutor = ret
                                Throw (New Exception("ERROR: Wait For Element operation failed:" & oObject & "Error:" & " Timeout occurred!"))
                            End If
                        Case "select"
                            If Commands.runCommand(oObject & ".select(""" & Commands.CustomEVal(sData) & """)", ret, errMsg) Then
                                If errMsg = "SUCCESS" Then
                                    AppCommandExecutor = ret
                                Else
                                    AppCommandExecutor = ret
                                    Throw (New Exception(oObject & ".select(""" & Commands.CustomEVal(sData) & """); ERROR:" & errMsg))
                                End If
                            Else
                                AppCommandExecutor = ret
                                Throw (New Exception("ERROR: select action is not performed on object:" & oObject & "Error:" & errMsg))
                            End If
                            'Case "selectcell"
                            '    If Commands.runCommand(oObject & ".Exist(1)", ret, errMsg) Then
                            '        If Commands.runCommand(oObject & ".selectCell(" & Commands.CustomEVal(sData) & ")", ret, errMsg) Then
                            '            If errMsg = "SUCCESS" Then
                            '                AppCommandExecutor = ret
                            '            Else
                            '                AppCommandExecutor = ret
                            '                Throw (New Exception(oObject & ".selectCell(" & Commands.CustomEVal(sData) & "); ERROR:" & errMsg))
                            '            End If
                            '        Else
                            '            AppCommandExecutor = ret
                            '            Throw (New Exception("ERROR: select action is not performed on object:" & oObject & "Error:" & errMsg))
                            '        End If
                            '    Else
                            '        AppCommandExecutor = ret
                            '        Throw (New Exception(oObject & ".Exist(1); ERROR: Object not found in application: " & errMsg))
                            '    End If
                        Case "check"
                            If Commands.runCommand(oObject & ".Exist(1)", ret, errMsg) Then
                                If Commands.runCommand(oObject & ".Set(""ON"")", ret, errMsg) Then
                                    If errMsg = "SUCCESS" Then
                                        AppCommandExecutor = ret
                                    Else
                                        AppCommandExecutor = ret
                                        Throw (New Exception(oObject & ".Set(""ON""); ERROR:" & errMsg))
                                    End If
                                Else
                                    AppCommandExecutor = ret
                                    Throw (New Exception("ERROR: Set(on) action is not performed on object:" & oObject & "Error:" & errMsg))
                                End If
                            Else
                                AppCommandExecutor = ret
                                Throw (New Exception(oObject & ".Exist(1); ERROR: Object not found in application: " & errMsg))
                            End If
                        Case "uncheck"
                            If Commands.runCommand(oObject & ".Exist(1)", ret, errMsg) Then
                                If Commands.runCommand(oObject & ".Set(""OFF"")", ret, errMsg) Then
                                    If errMsg = "SUCCESS" Then
                                        AppCommandExecutor = ret
                                    Else
                                        AppCommandExecutor = ret
                                        Throw (New Exception(oObject & ".Set(""OFF""); ERROR:" & errMsg))
                                    End If
                                Else
                                    AppCommandExecutor = ret
                                    Throw (New Exception("ERROR: Set(off) action is not performed on object:" & oObject & "Error:" & errMsg))
                                End If
                            Else
                                AppCommandExecutor = ret
                                Throw (New Exception(oObject & ".Exist(1); ERROR: Object not found in application: " & errMsg))
                            End If

                        Case "selectoption", "select"
                            If Commands.runCommand(oObject & ".select(""" & sData & """)", ret, errMsg) Then
                                If errMsg = "SUCCESS" Then
                                    AppCommandExecutor = ret
                                Else
                                    AppCommandExecutor = ret
                                    Throw (New Exception(oObject & ".select(""" & Commands.CustomEVal(sData) & """); ERROR:" & errMsg))
                                End If
                            Else
                                AppCommandExecutor = ret
                                Throw (New Exception("ERROR: select action is not performed on object:" & oObject & "Error:" & errMsg))
                            End If
                        Case "getoptioncount", "getallitemscount", "itemscount"
                            If Commands.runCommand(oObject & ".Exist(1)", ret, errMsg) Then
                                If Commands.runCommand(oObject & ".GetRoProperty(""items count"")", ret, errMsg) Then
                                    If errMsg = "SUCCESS" Then
                                        AppCommandExecutor = ret
                                    Else
                                        AppCommandExecutor = ret
                                        Throw (New Exception(oObject & ".GetRoProperty(""items count""); ERROR:" & errMsg))
                                    End If
                                Else
                                    AppCommandExecutor = ret
                                    Throw (New Exception("ERROR: GetRoProperty(""items count"") action is not performed on object:" & oObject & "Error:" & errMsg))
                                End If
                            Else
                                AppCommandExecutor = ret
                                Throw (New Exception(oObject & ".Exist(1); ERROR: Object not found in application: " & errMsg))
                            End If
                            '@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ New COMMMMNADNDS @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@

                            '@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ Tested Keywords Start @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
                        Case "selectoptionbyvalue"
                            If Commands.runCommand(oObject & ".Exist(1)", ret, errMsg) Then
                                sData = Commands.CustomEVal(sData)
                                If Commands.runCommand(oObject & ".SelectOptionByValue(""" & sData & """)", ret, errMsg) Then
                                    If errMsg = "SUCCESS" Then
                                        AppCommandExecutor = ret
                                    Else
                                        AppCommandExecutor = ret
                                        Throw (New Exception(oObject & ".SelectOptionByValue(""" & sData & """); ERROR:" & errMsg))
                                    End If
                                Else
                                    AppCommandExecutor = ret
                                    Throw (New Exception("ERROR: SelectOptionByValue action is not performed on object:" & oObject & "Error:" & errMsg))
                                End If
                            Else
                                AppCommandExecutor = ret
                                Throw (New Exception(oObject & ".Exist(1); ERROR: Object not found in application: " & errMsg))
                            End If

                        Case "selectoptionbylabel"
                            If Commands.runCommand(oObject & ".Exist(1)", ret, errMsg) Then
                                sData = Commands.CustomEVal(sData)
                                If Commands.runCommand(oObject & ".select(""" & sData & """)", ret, errMsg) Then
                                    If errMsg = "SUCCESS" Then
                                        AppCommandExecutor = ret
                                    Else
                                        AppCommandExecutor = ret
                                        Throw (New Exception(oObject & ".select(""" & sData & """); ERROR:" & errMsg))
                                    End If
                                Else
                                    AppCommandExecutor = ret
                                    Throw (New Exception("ERROR: selectoptionbylabel action is not performed on object:" & oObject & "Error:" & errMsg))
                                End If
                            Else
                                AppCommandExecutor = ret
                                Throw (New Exception(oObject & ".Exist(1); ERROR: Object not found in application: " & errMsg))
                            End If

                        Case "selectoptionbyindex"
                            If Commands.runCommand(oObject & ".Exist(1)", ret, errMsg) Then
                                sData = Commands.CustomEVal(sData - 1)
                                If Commands.runCommand(oObject & ".select(""#" & sData & """)", ret, errMsg) Then
                                    If errMsg = "SUCCESS" Then
                                        AppCommandExecutor = ret
                                    Else
                                        AppCommandExecutor = ret
                                        Throw (New Exception(oObject & ".select(""#""" & sData & """); ERROR:" & errMsg))
                                    End If
                                Else
                                    AppCommandExecutor = ret
                                    Throw (New Exception("ERROR: selectoptionbyindex action is not performed on object:" & oObject & "Error:" & errMsg))
                                End If
                            Else
                                AppCommandExecutor = ret
                                Throw (New Exception(oObject & ".Exist(1); ERROR: Object not found in application: " & errMsg))
                            End If

                            '$$$$$$$$$$$$$$$$$$$$$$$$$$$$ New Keywords developed on 12-Oct-2012 Start   $$$$$$$$$$$$$$$$$$$$$$$$$$$$
                        Case "selectlistoptions", "select listoptions"
                            If Commands.runCommand(oObject & ".Exist(1)", ret, errMsg) Then
                                sData = Commands.CustomEVal(sData)
                                If InStr(sData, ",") > 0 Then
                                    arrItemsToSelect = Split(sData, ",", -1, 1)
                                    For nItemCount = 0 To UBound(arrItemsToSelect)
                                        Commands.runCommand(oObject & ".ExtendSelect(""" & arrItemsToSelect(nItemCount) & """)", ret, errMsg)
                                    Next
                                    If errMsg = "SUCCESS" Then
                                        AppCommandExecutor = ret
                                    Else
                                        AppCommandExecutor = ret
                                        Throw (New Exception(oObject & ".ExtendSelect(""" & Commands.CustomEVal(sData) & """); ERROR:" & errMsg))
                                    End If
                                Else
                                    If Commands.runCommand(oObject & ".select(""" & sData & """)", ret, errMsg) Then
                                        If errMsg = "SUCCESS" Then
                                            AppCommandExecutor = ret
                                        Else
                                            AppCommandExecutor = ret
                                            Throw (New Exception(oObject & ".select(""" & Commands.CustomEVal(sData) & """); ERROR:" & errMsg))
                                        End If
                                    Else
                                        AppCommandExecutor = ret
                                        Throw (New Exception("ERROR: selectlistoptions action is not performed on object:" & oObject & "Error:" & errMsg))
                                    End If
                                End If
                            Else
                                AppCommandExecutor = ret
                                Throw (New Exception(oObject & ".Exist(1); ERROR: Object not found in application: " & errMsg))
                            End If
                            '$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$Keyword developed on 15-Oct-2012 Start $$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$
                        Case "unselectlistoptions", "unselect listoptions"
                            If Commands.runCommand(oObject & ".Exist(1)", ret, errMsg) Then
                                sData = Commands.CustomEVal(sData)
                                If QTP._browserType = "firefox" Or QTP._browserType = "*firefox" Or QTP._browserType = "ff" Or QTP._browserType = "*ff" Then
                                    If Commands.runCommand(oObject & ".UnselectListOptionsInFirefox(""" & sData & """)", ret, errMsg) Then
                                        If errMsg = "SUCCESS" Then
                                            AppCommandExecutor = ret
                                        Else
                                            AppCommandExecutor = ret
                                            Throw (New Exception(oObject & ".UnselectListOptions(""" & sData & """); ERROR:" & errMsg))
                                        End If
                                    Else
                                        AppCommandExecutor = ret
                                        Throw (New Exception("ERROR: UnselectListOptions action is not performed in Firefox Browser on object:" & oObject & "Error:" & errMsg))
                                    End If
                                Else
                                    If Commands.runCommand(oObject & ".UnselectListOptions(""" & sData & """)", ret, errMsg) Then
                                        If errMsg = "SUCCESS" Then
                                            AppCommandExecutor = ret
                                        Else
                                            AppCommandExecutor = ret
                                            Throw (New Exception(oObject & ".UnselectListOptions(""" & sData & """); ERROR:" & errMsg))
                                        End If
                                    Else
                                        AppCommandExecutor = ret
                                        Throw (New Exception("ERROR: UnselectListOptions action is not performed otherthan Firefox Browser on object:" & oObject & "Error:" & errMsg))
                                    End If

                                End If
                            Else
                                AppCommandExecutor = ret
                                Throw (New Exception(oObject & ".Exist(1); ERROR: Object not found in application: " & errMsg))
                            End If
                        Case "unselectalllistoptions", "unselect alllistoptions"
                            If Commands.runCommand(oObject & ".Exist(1)", ret, errMsg) Then
                                'sData = Commands.CustomEVal(sData)
                                If QTP._browserType = "firefox" Or QTP._browserType = "*firefox" Or QTP._browserType = "ff" Or QTP._browserType = "*ff" Then
                                    If Commands.runCommand(oObject & ".UnselectAllListOptionsInFirefox()", ret, errMsg) Then
                                        If errMsg = "SUCCESS" Then
                                            AppCommandExecutor = ret
                                        Else
                                            AppCommandExecutor = ret
                                            Throw (New Exception(oObject & ".UnselectAllListOptions(); ERROR:" & errMsg))
                                        End If
                                    Else
                                        AppCommandExecutor = ret
                                        Throw (New Exception("ERROR: UnselectAllListOptions action is not performed in Firefox Browser on object:" & oObject & "Error:" & errMsg))
                                    End If
                                Else
                                    If Commands.runCommand(oObject & ".UnselectAllListOptions()", ret, errMsg) Then
                                        If errMsg = "SUCCESS" Then
                                            AppCommandExecutor = ret
                                        Else
                                            AppCommandExecutor = ret
                                            Throw (New Exception(oObject & ".UnselectAllListOptions(); ERROR:" & errMsg))
                                        End If
                                    Else
                                        AppCommandExecutor = ret
                                        Throw (New Exception("ERROR: UnselectAllListOptions action is not performed otherthan Firefox Browser on object:" & oObject & "Error:" & errMsg))
                                    End If
                                End If
                            Else
                                AppCommandExecutor = ret
                                Throw (New Exception(oObject & ".Exist(1); ERROR: Object not found in application: " & errMsg))
                            End If

                        Case "gettitle", "get title"
                            If Commands.runCommand(psRootObject & ".GetROProperty(""title"")", ret, errMsg) Then
                                AppCommandExecutor = ret
                            Else
                                AppCommandExecutor = ret
                            End If

                        Case "getconfirmation", "get confirmation"
                            Dim sAlertMsg
                            If Commands.runCommand(oObject & ".Exist(1)", ret, errMsg) Then
                                Commands.runCommand(psRootObject & "Dialog(""Class Name:=Dialog"").Static(""Class Name:=Static"",""index:=0"").GetROProperty(""Text"")", ret, errMsg)
                                If ret = "" Then
                                    Commands.runCommand(psRootObject & "Dialog(""Class Name:=Dialog"").Static(""Class Name:=Static"",""index:=1"").GetROProperty(""Text"")", ret, errMsg)
                                    sAlertMsg = ret
                                Else
                                    sAlertMsg = ""
                                End If
                                If Commands.runCommand(oObject & ".click(1)", ret, errMsg) Then
                                    If errMsg = "SUCCESS" Then
                                        AppCommandExecutor = sAlertMsg
                                    Else
                                        AppCommandExecutor = sAlertMsg
                                        Throw (New Exception(oObject & ".Click(); ERROR:" & errMsg))
                                    End If
                                Else
                                    AppCommandExecutor = ret
                                    Throw (New Exception("ERROR: Click action is not performed on object:" & oObject & "Error:" & errMsg))
                                End If
                            Else
                                AppCommandExecutor = ret
                                Throw (New Exception(oObject & ".Exist(1); ERROR: Object not found in application: " & errMsg))
                            End If
                            '$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$Keyword developed on 15-Oct-2012 End $$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$
                            '++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
                        Case "verifytextnotpresent", "verify textnotpresent"
                            If Commands.runCommand(oObject & ".Exist(1)", ret, errMsg) Then
                                sData = Commands.CustomEVal(sData)
                                If Commands.runCommand(oObject & ".GetROProperty(""innertext"")", ret, errMsg) Then
                                    If errMsg = "SUCCESS" Then
                                        If InStr(LCase(sData), LCase(ret)) = 0 Then
                                            AppCommandExecutor = "True"
                                        Else
                                            AppCommandExecutor = "False"
                                        End If
                                    Else
                                        AppCommandExecutor = ret
                                        Throw (New Exception("Action:verifytextnotpresent; Data:" & sData & "; ERROR:" & errMsg))
                                    End If
                                Else
                                    AppCommandExecutor = ret
                                    Throw (New Exception("ERROR: GetROProperty(""innertext"") action is not performed on object:" & oObject & "Error:" & errMsg))
                                End If
                            Else
                                AppCommandExecutor = ret
                                Throw (New Exception(oObject & ".Exist(1); ERROR: Object not found in application: " & errMsg))
                            End If
                            '++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
                        Case "getselectoptionbyvalue"
                            If Commands.runCommand(oObject & ".Exist(1)", ret, errMsg) Then
                                sData = Commands.CustomEVal(sData)
                                If Commands.runCommand(oObject & ".getSelectOptionByValue(""" & sData & """)", ret, errMsg) Then
                                    If errMsg = "SUCCESS" Then
                                        AppCommandExecutor = ret
                                    Else
                                        AppCommandExecutor = ret
                                        Throw (New Exception(oObject & ".getSelectOptionByValue(""" & sData & """); ERROR:" & errMsg))
                                    End If
                                Else
                                    AppCommandExecutor = ret
                                    Throw (New Exception("ERROR: getSelectOptionByValue action is not performed on object:" & oObject & "Error:" & errMsg))
                                End If
                            Else
                                AppCommandExecutor = ret
                                Throw (New Exception(oObject & ".Exist(1); ERROR: Object not found in application: " & errMsg))
                            End If

                        Case "getselectoptionbylabel"
                            If Commands.runCommand(oObject & ".Exist(1)", ret, errMsg) Then
                                sData = Commands.CustomEVal(sData)
                                If Commands.runCommand(oObject & ".GetRoProperty(""selection"")", ret, errMsg) Then
                                    If errMsg = "SUCCESS" Then
                                        AppCommandExecutor = ret
                                    Else
                                        AppCommandExecutor = ret
                                        Throw (New Exception(oObject & ".GetRoProperty(""selection""); ERROR:" & errMsg))
                                    End If
                                Else
                                    AppCommandExecutor = ret
                                    Throw (New Exception("ERROR: getselectoptionbylabel action is not performed on object:" & oObject & "Error:" & errMsg))
                                End If
                            Else
                                AppCommandExecutor = ret
                                Throw (New Exception(oObject & ".Exist(1); ERROR: Object not found in application: " & errMsg))
                            End If

                        Case "getselectoptionbyindex"
                            If Commands.runCommand(oObject & ".Exist(1)", ret, errMsg) Then
                                sData = Commands.CustomEVal(sData)
                                If Commands.runCommand(oObject & ".GetRoProperty(""selected item index"")", ret, errMsg) Then
                                    If errMsg = "SUCCESS" Then
                                        AppCommandExecutor = ret
                                    Else
                                        AppCommandExecutor = ret
                                        Throw (New Exception(oObject & ".GetRoProperty(""selected item index""); ERROR:" & errMsg))
                                    End If
                                Else
                                    AppCommandExecutor = ret
                                    Throw (New Exception("ERROR: getselectoptionbyindex action is not performed on object:" & oObject & "Error:" & errMsg))
                                End If
                            Else
                                AppCommandExecutor = ret
                                Throw (New Exception(oObject & ".Exist(1); ERROR: Object not found in application: " & errMsg))
                            End If

                            '@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ Tested Keywords end @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@

                            '@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ New Keywords developed by pmadisetti as on 30Aug2012 start @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
                        Case "verifystate", "verify state"
                            If Commands.runCommand(oObject & ".Exist(1)", ret, errMsg) Then
                                sData = Commands.CustomEVal(sData)
                                If LCase(Trim(sData)) = "visible" Then
                                    Commands.runCommand(oObject & ".GetROProperty(""visible"")", ret, errMsg)
                                    If LCase(Trim(ret)) = True Then
                                        AppCommandExecutor = "true"
                                    Else
                                        AppCommandExecutor = "false"
                                    End If
                                ElseIf LCase(Trim(sData)) = "enabled" Then
                                    Commands.runCommand(oObject & ".GetROProperty(""disabled"")", ret, errMsg)
                                    If LCase(Trim(ret)) = 0 Then
                                        AppCommandExecutor = "true"
                                    Else
                                        AppCommandExecutor = "false"
                                    End If
                                ElseIf LCase(Trim(sData)) = "disabled" Then
                                    Commands.runCommand(oObject & ".GetROProperty(""disabled"")", ret, errMsg)
                                    If LCase(Trim(ret)) = 1 Then
                                        AppCommandExecutor = "true"
                                    Else
                                        AppCommandExecutor = "false"
                                    End If
                                End If
                            Else
                                AppCommandExecutor = ret
                                Throw (New Exception(oObject & ".Exist(1); ERROR: Object not found in application: " & errMsg))
                            End If

                        Case "verifyvalue", "verify value"
                            If Commands.runCommand(oObject & ".Exist(1)", ret, errMsg) Then
                                sData = Commands.CustomEVal(sData)
                                Commands.runCommand(oObject & ".GetROProperty(""value"")", ret, errMsg)
                                If LCase(Trim(ret)) = LCase(Trim(sData)) Then
                                    AppCommandExecutor = "true"
                                Else
                                    AppCommandExecutor = "false"
                                End If
                            Else
                                AppCommandExecutor = ret
                                Throw (New Exception(oObject & ".Exist(1); ERROR: Object not found in application: " & errMsg))
                            End If

                        Case "verifyselectoptions", "verify select options", "verify selectoptions"
                            If Commands.runCommand(oObject & ".Exist(1)", ret, errMsg) Then
                                sData = Commands.CustomEVal(sData)
                                Commands.runCommand(oObject & ".GetROProperty(""all items"")", ret, errMsg)
                                arrAllDropdownItems = Split(ret, ";", -1, 1)
                                arrVerifyItemsInDropDown = Split(sData, ",", -1, 1)
                                nItemCountFlag = 0
                                For iVerifyItemIndex = 0 To UBound(arrVerifyItemsInDropDown)
                                    For iItemIndex = 0 To UBound(arrAllDropdownItems)
                                        If arrVerifyItemsInDropDown(iVerifyItemIndex) = arrAllDropdownItems(iItemIndex) Then
                                            nItemCountFlag = nItemCountFlag + 1
                                            Exit For
                                        End If
                                    Next
                                Next
                                If nItemCountFlag = UBound(arrVerifyItemsInDropDown) + 1 Then
                                    If errMsg = "SUCCESS" Then
                                        AppCommandExecutor = "true"
                                    Else
                                        AppCommandExecutor = "false"
                                        Throw (New Exception("verifyselectoptions : " & sData & " on Combobox or Drop Down object:" & oObject & " ; ERROR:" & errMsg))
                                    End If
                                Else
                                    AppCommandExecutor = ret
                                    Throw (New Exception("ERROR: verifyselectoptions:" & sData & "on Combobox or Drop Down object:" & oObject & " is failed ErrorMsg:" & errMsg))
                                End If
                            Else
                                AppCommandExecutor = ret
                                Throw (New Exception(oObject & ".Exist(1); ERROR: Object not found in application: " & errMsg))
                            End If
                            '@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ New Keywords developed by pm end @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@

                        Case "clickforfilebrowse"
                            If Commands.runCommand(oObject & ".Exist(1)", ret, errMsg) Then
                                If Commands.runCommand(oObject & ".Click()", ret, errMsg) Then
                                    If errMsg = "SUCCESS" Then
                                        AppCommandExecutor = ret
                                    Else
                                        AppCommandExecutor = ret
                                        Throw (New Exception(oObject & ".Click(); ERROR:" & errMsg))
                                    End If
                                Else
                                    AppCommandExecutor = ret
                                    Throw (New Exception("ERROR: Click action is not performed on object:" & oObject & "Error:" & errMsg))
                                End If
                            Else
                                AppCommandExecutor = ret
                                Throw (New Exception(oObject & ".Exist(1); ERROR: Object not found in application: " & errMsg))
                            End If
                        Case "cleartext"
                            If Commands.runCommand(oObject & ".Exist(1)", ret, errMsg) Then
                                If Commands.runCommand(oObject & ".Set("""")", ret, errMsg) Then
                                    If errMsg = "SUCCESS" Then
                                        AppCommandExecutor = ret
                                    Else
                                        AppCommandExecutor = ret
                                        Throw (New Exception(oObject & ".Set(""""); ERROR:" & errMsg))
                                    End If
                                Else
                                    AppCommandExecutor = ret
                                    Throw (New Exception("ERROR: clear text action is not performed on object:" & oObject & "Error:" & errMsg))
                                End If
                            Else
                                AppCommandExecutor = ret
                                Throw (New Exception(oObject & ".Exist(1); ERROR: Object not found in application: " & errMsg))
                            End If

                        Case "getattribute"
                            If Commands.runCommand(oObject & ".Exist(1)", ret, errMsg) Then
                                sData = Commands.CustomEVal(sData)
                                If Commands.runCommand(oObject & ".GetRoProperty(""" & sData & """)", ret, errMsg) Then
                                    If errMsg = "SUCCESS" Then
                                        AppCommandExecutor = ret
                                    Else
                                        AppCommandExecutor = ret
                                        Throw (New Exception(oObject & ".GetRoProperty(""" & sData & """); ERROR:" & errMsg))
                                    End If
                                Else
                                    AppCommandExecutor = ret
                                    Throw (New Exception("ERROR: getAttribute action is not performed on object:" & oObject & "Error:" & errMsg))
                                End If
                            Else
                                AppCommandExecutor = ret
                                Throw (New Exception(oObject & ".Exist(1); ERROR: Object not found in application: " & errMsg))
                            End If
                            'Case "getrowid"
                            '    If Commands.runCommand(oObject & ".Exist(1)", ret, errMsg) Then
                            '        sData = Commands.CustomEVal(sData)
                            '        If Commands.runCommand(oObject & ".GetRowWithCellText(""" & sData & """)", ret, errMsg) Then
                            '            If errMsg = "SUCCESS" Then
                            '                AppCommandExecutor = ret
                            '            Else
                            '                AppCommandExecutor = ret
                            '                Throw (New Exception(oObject & ".GetRowWithCellText(""" & sData & """); ERROR:" & errMsg))
                            '            End If
                            '        Else
                            '            AppCommandExecutor = ret
                            '            Throw (New Exception("ERROR: getRowIndex action is not performed on object:" & oObject & "Error:" & errMsg))
                            '        End If
                            '    Else
                            '        AppCommandExecutor = ret
                            '        Throw (New Exception(oObject & ".Exist(1); ERROR: Object not found in application: " & errMsg))
                            '    End If

                            'Case "getcolumnid"
                            '    If Commands.runCommand(oObject & ".Exist(1)", ret, errMsg) Then
                            '        Commands.runCommand(oObject & ".GetROProperty(""column names"")", ret, errMsg)
                            '        sData = Commands.CustomEVal(sData)
                            '        arrColumNames = Split(ret, ";", -1, 1)
                            '        For intLoopIndex = 0 To UBound(arrColumNames)
                            '            If arrColumNames(intLoopIndex) = sData Then
                            '                ret = intLoopIndex + 1
                            '                columindexFlag = True
                            '                Exit For
                            '            End If
                            '        Next intLoopIndex
                            '        If columindexFlag Then
                            '            If errMsg = "SUCCESS" Then
                            '                AppCommandExecutor = ret
                            '            Else
                            '                AppCommandExecutor = ret
                            '                Throw (New Exception("getcolumnindex of Column name: " & sData & " in WebTable object:" & oObject & " ; ERROR:" & errMsg))
                            '            End If
                            '        Else
                            '            AppCommandExecutor = ret
                            '            Throw (New Exception("ERROR: column name:" & sData & "is does not exist in table" & errMsg))
                            '        End If
                            '    Else
                            '        AppCommandExecutor = ret
                            '        Throw (New Exception(oObject & ".Exist(1); ERROR: Object not found in application: " & errMsg))
                            '    End If

                        Case "doubleclick"
                            If Commands.runCommand(oObject & ".Exist(1)", ret, errMsg) Then
                                sData = Commands.CustomEVal(sData)
                                If Commands.runCommand(oObject & ".FireEvent ""ondblclick""", ret, errMsg) Then
                                    If errMsg = "SUCCESS" Then
                                        AppCommandExecutor = ret
                                    Else
                                        AppCommandExecutor = ret
                                        Throw (New Exception(oObject & ".FireEvent ""ondblclick""; ERROR:" & errMsg))
                                    End If
                                Else
                                    AppCommandExecutor = ret
                                    Throw (New Exception("ERROR: doubleClick action is not performed on object:" & oObject & "Error:" & errMsg))
                                End If
                            Else
                                AppCommandExecutor = ret
                                Throw (New Exception(oObject & ".Exist(1); ERROR: Object not found in application: " & errMsg))
                            End If
                        Case "getselectedoptioncount"
                            If Commands.runCommand(oObject & ".Exist(1)", ret, errMsg) Then
                                If Commands.runCommand(oObject & ".GetRoProperty(""selected items count"")", ret, errMsg) Then
                                    If errMsg = "SUCCESS" Then
                                        AppCommandExecutor = ret
                                    Else
                                        AppCommandExecutor = ret
                                        Throw (New Exception(oObject & ".GetRoProperty(""selected items count""); ERROR:" & errMsg))
                                    End If
                                Else
                                    AppCommandExecutor = ret
                                    Throw (New Exception("ERROR: getSelectedOptionCount action is not performed on object:" & oObject & "Error:" & errMsg))
                                End If
                            Else
                                AppCommandExecutor = ret
                                Throw (New Exception(oObject & ".Exist(1); ERROR: Object not found in application: " & errMsg))
                            End If

                        Case "gettext"
                            If Commands.runCommand(oObject & ".Exist(1)", ret, errMsg) Then
                                If Commands.runCommand(oObject & ".GetROProperty(""value"")", ret, errMsg) Then
                                    If errMsg = "SUCCESS" Then
                                        If ret = "" Then Commands.runCommand(oObject & ".GetRoProperty(""text"")", ret, errMsg) 'for some we get text from text attribute
                                        AppCommandExecutor = ret
                                    Else
                                        AppCommandExecutor = ret
                                        Throw (New Exception(oObject & ".GetROProperty(""value""); ERROR:" & errMsg))
                                    End If
                                Else
                                    AppCommandExecutor = ret
                                    Throw (New Exception("ERROR: getText action is not performed on object:" & oObject & "Error:" & errMsg))
                                End If
                            Else
                                AppCommandExecutor = ret
                                Throw (New Exception(oObject & ".Exist(1); ERROR: Object not found in application: " & errMsg))
                            End If
                            'Case "waitforelementpresent"

                        Case "getvalue"
                            If Commands.runCommand(oObject & ".Exist(1)", ret, errMsg) Then
                                If Commands.runCommand(oObject & ".GetROProperty(""value"")", ret, errMsg) Then
                                    If errMsg = "SUCCESS" Then
                                        AppCommandExecutor = ret
                                    Else
                                        AppCommandExecutor = ret
                                        Throw (New Exception(oObject & ".GetROProperty(""value""); ERROR:" & errMsg))
                                    End If
                                Else
                                    AppCommandExecutor = ret
                                    Throw (New Exception("ERROR: getValue action is not performed on object:" & oObject & "Error:" & errMsg))
                                End If
                            Else
                                AppCommandExecutor = ret
                                Throw (New Exception(oObject & ".Exist(1); ERROR: Object not found in application: " & errMsg))
                            End If
                            'Case "gettablecolumncount"
                            '    If Commands.runCommand(oObject & ".Exist(1)", ret, errMsg) Then
                            '        If Commands.runCommand(oObject & ".GetROProperty(""cols"")", ret, errMsg) Then
                            '            If errMsg = "SUCCESS" Then
                            '                AppCommandExecutor = ret
                            '            Else
                            '                AppCommandExecutor = ret
                            '                Throw (New Exception(oObject & ".GetROProperty(""cols"") ERROR: " & errMsg))
                            '            End If
                            '        Else
                            '            AppCommandExecutor = ret
                            '            Throw (New Exception("ERROR: getTableColumnCount action is not performed on object:" & oObject & "Error:" & errMsg))
                            '        End If
                            '    Else
                            '        AppCommandExecutor = ret
                            '        Throw (New Exception(oObject & ".Exist(1); ERROR: Object not found in application: " & errMsg))
                            '    End If
                        Case "verifyelement"
                            If Commands.runCommand(oObject & ".Exist(1)", ret, errMsg) Then
                                If LCase(Trim(sData)) = "true" Then
                                    AppCommandExecutor = "true"
                                Else
                                    AppCommandExecutor = "false"
                                End If
                            Else
                                If LCase(Trim(sData)) = "false" Then
                                    AppCommandExecutor = "true"
                                Else
                                    AppCommandExecutor = "false"
                                End If
                            End If
                        Case "verifytext", "istextpresent", "istext present"
                            If Commands.runCommand(oObject & ".Exist(1)", ret, errMsg) Then
                                Commands.runCommand(oObject & ".GetROProperty(""innertext"")", ret, errMsg)
                                'If LCase(Trim(sData)) = LCase(Trim(ret)) Then
                                If InStr(LCase(Trim(sData)), LCase(Trim(ret)), CompareMethod.Text) > 0 Then
                                    AppCommandExecutor = "true"
                                Else
                                    AppCommandExecutor = "false"
                                End If
                            Else
                                AppCommandExecutor = "false"
                            End If
                        Case "setitemstate"
                            Dim aData(1) As String
                            Dim sCmd As String
                            aData = Split(sData, ",")

                            If LCase(aData(1).Trim()) = "true" Then
                                Commands.runCommand(oObject & ".SetItemState(""" & aData(0) & """, True)", ret, errMsg)
                            Else
                                Commands.runCommand(oObject & ".SetItemState(""" & aData(0) & """, False)", ret, errMsg)
                            End If
                        Case "activatetreenode"
                            Dim aData(1) As String
                            Dim sCmd As String
                            aData = Split(sData, ",")
                            If UBound(aData) = 1 Then
                                Commands.runCommand(oObject & ".Activate(""" & aData(0) & """, " & aData(1) & ")", ret, errMsg)
                            Else
                                Commands.runCommand(oObject & ".Activate(""" & aData(0) & """)", ret, errMsg)
                            End If
                        Case "getsubitem"
                            Dim aData(1) As String
                            Dim sCmd As String
                            aData = Split(sData, ",")
                            If UBound(aData) = 1 Then
                                Commands.runCommand(oObject & ".getsubitem(""" & aData(0) & """, " & aData(1) & ")", ret, errMsg)
                                AppCommandExecutor = ret
                            Else
                                AppCommandExecutor = ret
                            End If
                        Case "getitem"
                            If Commands.runCommand(oObject & ".Exist(1)", ret, errMsg) Then
                                AppCommandExecutor = "true"
                            Else
                                AppCommandExecutor = "false"
                            End If
                            Commands.runCommand(oObject & ".GetItem(" & sData & ")", ret, errMsg)
                            AppCommandExecutor = ret
                        Case "verifyelementwithtext"
                            Commands.runCommand(oObject & ".SetToProperty(""innertext"", """ & sData & """)", ret, errMsg)
                            If Commands.runCommand(oObject & ".Exist(1)", ret, errMsg) Then
                                AppCommandExecutor = "true"
                            Else
                                AppCommandExecutor = "false"
                            End If
                            'Case "gettablerowcount"
                            '    If Commands.runCommand(oObject & ".Exist(1)", ret, errMsg) Then
                            '        If Commands.runCommand(oObject & ".GetROProperty(""rows"")", ret, errMsg) Then
                            '            If errMsg = "SUCCESS" Then
                            '                AppCommandExecutor = ret
                            '            Else
                            '                AppCommandExecutor = ret
                            '                Throw (New Exception(oObject & ".GetROProperty(""rows""); ERROR:" & errMsg))
                            '            End If
                            '        Else
                            '            AppCommandExecutor = ret
                            '            Throw (New Exception("ERROR: getTableRowCount action is not performed on object:" & oObject & "Error:" & errMsg))
                            '        End If
                            '    Else
                            '        AppCommandExecutor = ret
                            '        Throw (New Exception(oObject & ".Exist(1); ERROR: Object not found in application: " & errMsg))
                            '    End If
                        Case "getroproperty"
                            If Commands.runCommand(oObject & ".Exist(1)", ret, errMsg) Then
                                If Commands.runCommand(oObject & ".GetRoProperty(""" & sData & """)", ret, errMsg) Then '
                                    If errMsg = "SUCCESS" Then
                                        AppCommandExecutor = ret
                                    Else
                                        AppCommandExecutor = ret
                                        Throw (New Exception(oObject & ".GetRoProperty(" & sData & "); ERROR:" & errMsg))
                                    End If
                                Else
                                    AppCommandExecutor = ret
                                    Throw (New Exception("ERROR: GetRoProperty(" & sData & ") action is not performed on object:" & oObject & "Error:" & errMsg))
                                End If
                            Else
                                AppCommandExecutor = ret
                                Throw (New Exception(oObject & ".Exist(1); ERROR: Object not found in application: " & errMsg))
                            End If
                        Case Else 'unknown object-Action
                            qtpObject = ""
                            sCommand = ""
                            qtpObject = Right(oObject, Len(oObject) - InStrRev(oObject, "."))
                            qtpObject = Left(qtpObject, InStr(qtpObject, "(") - 1)
                            sCommand = CommonConfig.getActionCommand(xAFTObject, qtpObject, sAction, sData)

                            If Left(sAction, 3) = "get" Then
                                AppCommandExecutor = Commands.CustomEVal(sData & "=""" & Commands.CustomEVal(oObject & ".GetRoProperty(" & Mid(sAction, 4) & ")") & """")
                            ElseIf sCommand = "" Then
                                'Unknown command
                                Throw (New Exception(
                                       "Unknown Object: Couldnt find xAFTObject-QTPObject mapping for the following object type and action: " & _
                                        "qtpObject- " + qtpObject & " " & _
                                        "xAFTObject- " + xAFTObject & " " & _
                                        "xAFTAction- " + sAction & " "
                                        ))
                            Else
                                If Commands.runCommand(oObject & "." & sCommand, ret, errMsg) Then '
                                    If errMsg = "SUCCESS" Then
                                        AppCommandExecutor = ret
                                    Else
                                        AppCommandExecutor = ret
                                        Throw (New Exception(oObject & "." & sCommand & "); ERROR:" & errMsg))
                                    End If
                                Else
                                    AppCommandExecutor = ret
                                    Throw (New Exception("ERROR: " & sCommand & "action is not performed on object:" & oObject & "Error:" & errMsg))
                                End If
                            End If

                            'If Left(sAction, 3) = "get" Then
                            '    AppCommandExecutor = Commands.CustomEVal(sData & "=""" & Commands.CustomEVal(oObject & ".GetRoProperty(" & Mid(sAction, 4) & ")") & """")
                            '    'Execute(sData & "=""" & Trim(oObject.GetRoProperty(Mid(sAction, 4))) & """")
                            '    'tried for getROproperty
                            '    'AppCommandExecutor = Commands.CustomEVal(Commands.CustomEVal(oObject & ".GetRoProperty(" & sData & ")"))
                            'Else
                            '    'may be user defined method; Try run it, throw exception if it fails
                            '    Dim aTempArr
                            '    'Commands.runCommand("Print ""Action: " & sActionName & "- Object not found! " & sObject & """", ret, errMsg)
                            '    If InStr(sData, ",") > 0 Then
                            '        aTempArr = Split(sData, ",")
                            '        If Commands.runCommand(oObject & "." & sActionName + "(""""" + aTempArr(0) + """"",""""" + aTempArr(1) + """"")", ret, errMsg) Then '
                            '            AppCommandExecutor = ret
                            '        End If
                            '    ElseIf Len(sData) = 0 Then
                            '        If Commands.runCommand(oObject & "." & sActionName + "()", ret, errMsg) Then '
                            '            AppCommandExecutor = ret
                            '        End If
                            '    ElseIf Commands.runCommand(oObject & "." & sActionName + "(""" + sData + """)", ret, errMsg) Then '
                            '        AppCommandExecutor = ret
                            '    Else
                            '        Throw (New Exception("Object not found / Unknown Action: OBJECT:" & sObject & " ACTION:" & sActionName & "; SOURCE: xAFTQTPEngine.Core.AppCommandExecutor"))
                            '    End If

                            '    ' If Commands.runCommand(oObject & "." & sActionName + "(" + sData + ")", ret, errMsg) Then '
                            '    'AppCommandExecutor = ret
                            '    ' Else
                            '    'Throw (New Exception("Object not found / Unknown Action: OBJECT:" & sObject & " ACTION:" & sActionName & "; SOURCE: xAFTQTPEngine.Core.AppCommandExecutor"))
                            'End If
                            ''   If Commands.runCommand(oObject & "." & sActionName + "(""" + sData + """)", ret, errMsg) Then '
                            'AppCommandExecutor = ret
                            '   Else
                            ' Throw (New Exception("Object not found / Unknown Action: OBJECT:" & sObject & " ACTION:" & sActionName & "; SOURCE: xAFTQTPEngine.Core.AppCommandExecutor"))
                            'End If


                    End Select
                    If errMsg <> "" And errMsg <> "SUCCESS" Then
                        MsgBox("MISSING ERRORS:: Object: " & sObject & "Action: " & sActionName & " -Error: " & errMsg) 'Temp msg to see if there are any missed commands/exceptions
                        'Throw (New Exception("Object: " & sObject & "Action: " & sActionName & " -Error: " & errMsg)) 'raise any missed exceptions
                    End If
                    ''*****                End If ''  WebTable Condition if Statement end
                    'End If
                    psLastObject = oObject 'holding this in public var so tht nxt action cmd can use when needed
                    'Next
                End If
        End Select
    End Function
    Function Annotation(ByVal sData)
        'MsgBox("Annotation: " & sData)
        Dim sSheet, sName, nIndex
        sData = LCase(Trim(sData))
        Select Case Left(sData, 2)
            Case "${"
                sSheet = Mid(sData, 3, InStr(sData, ".") - 4)
                sSheet = RecursiveFilter(sSheet, "'", "")
                sName = Mid(sData, InStr(sData, ".") + 1)
                sName = Left(sName, InStr(sName, "}$") - 1)
                sName = RecursiveFilter(sName, "'", "")
                Annotation = paoTestData(GetTestDataObjIndex(sSheet)).GetTestData(sName)
                'MsgBox("1 END OF Annotation: " & sData)
            Case "@s", "@S"
                sSheet = Mid(sData, InStr(sData, "=") + 1)
                sData = Mid(sSheet, InStr(sSheet, ",") + 1)
                sSheet = Left(sSheet, InStr(sSheet, ",") - 1)
                sSheet = RecursiveFilter(sSheet, "'", "")
                sData = RecursiveFilter(sData, "'", "")
                'MsgBox(">>>>" & UBound(Core.paoTestData))

                'MsgBox("Annotation sData: " & sData & " GetTestDataObjIndex(sSheet):" & GetTestDataObjIndex(sSheet))
                nIndex = GetTestDataObjIndex(sSheet)
                'MsgBox(">>>>" & UBound(Core.paoTestData))
                Core.paoTestData(nIndex).SetRow(CInt(sData) + 1)
                'MsgBox(">>>>" & UBound(Core.paoTestData))
                Annotation = ""
            Case Else
                Annotation = ""
                'MsgBox("2 END OF Annotation: " & sData)
        End Select

    End Function
    '@Pradeep, July 7th 2012: for duplicate elements finding issues
    Public Function GetTSREquivalentForXMLOR(ByVal sORXMLfilePath) 'retunr tsr equivalent for XML OR
        Dim nIndex
        For nIndex = 0 To UBound(QTPConfig.psORs)
            If Trim(LCase(sORXMLfilePath)) = LCase(Trim(QTPConfig.psORs(nIndex))) Then
                'Found transformed xml file from Env class
                'Return TSR equivalent
                GetTSREquivalentForXMLOR = QTP._ORArray(nIndex)
                Exit Function
            End If
        Next
        GetTSREquivalentForXMLOR = "No equivalent TSR found for following xml OR: " & sORXMLfilePath
    End Function
    '@Pradeep, July 7th 2012: for duplicate elements finding issues
    Public Function GetXMLEquivalentOfTSROR(ByVal sTSRORfilePath) 'retunr tsr equivalent for XML OR
        Dim nIndex
        For nIndex = 0 To UBound(QTP._ORArray)
            If Trim(LCase(sTSRORfilePath)) = LCase(Trim(QTP._ORArray(nIndex))) Then
                'Found transformed xml file from Env class
                'Return TSR equivalent
                GetXMLEquivalentOfTSROR = QTPConfig.psORs(nIndex)
                Exit Function
            End If
        Next
        GetXMLEquivalentOfTSROR = "No equivalent XML found for following TSR OR: " & sTSRORfilePath
    End Function

    Public Function GenerateCommonORXML() 'This is called at ENV.init initialization. It throws exception if it sees any duplidate elements
        Dim oNodeCollection, oNode, bPageFound, sTempPage
        Dim aoORObjects(-1), bDuplicate
        Dim oCommonORXml : oCommonORXml = CreateObject("Msxml2.DOMDocument.3.0")
        Dim oRoot, oChild, oXMLGroupNode, asDuplicateElements(-1), sDuplicateElements, oDuplicateNode
        oCommonORXml.appendChild(oCommonORXml.createElement("root"))
        oRoot = oCommonORXml.documentElement
        'xmldoc.documentElement.appendChild(xmldoc.createProcessingInstruction("xml", "version="""1.0""))

        Dim xmldoc As Object
        Dim sNodeName = ""
        sTempPage = ""
        For Each sXML In QTPConfig.psORs 'Loop through all associated OR's
            'MsgBox(sXML)
            xmldoc = CreateObject("Msxml2.DOMDocument.3.0")
            xmldoc.setProperty("SelectionLanguage", "XPath")
            xmldoc.setProperty("SelectionNamespaces", "xmlns:qtpRep='http://www.mercury.com/qtp/ObjectRepository'")

            xmldoc.load(sXML)


            xmldoc.async = False
            oNodeCollection = xmldoc.selectNodes("//qtpRep:Object") 'Get the object collection using XPath
            oXMLGroupNode = oCommonORXml.createElement("or")
            oXMLGroupNode.setAttribute("ref", sXML)
            oXMLGroupNode.setAttribute("active", "yes")

            bPageFound = False 'It will be set to true if current object has page in its hierarchy
            'MsgBox(oObjectName)
            sDuplicateElements = ""
            For Each oNode In oNodeCollection
                sNodeName = UCase(Trim(oNode.GetAttribute("Name")))
                oDuplicateNode = oCommonORXml.SelectSingleNode("//or[@active='yes']/element[@elementName='" & sNodeName & "']")
                If oDuplicateNode Is Nothing Then
                    'its not duplicate

                    oChild = oCommonORXml.createElement("element")
                    oChild.setAttribute("elementName", sNodeName)
                    bDuplicate = False 'This is set to true if any duplidates in below function call
                    oChild.text = GetHierarchy(sXML, oNode.GetAttribute("Name"), bDuplicate)
                    If bDuplicate Then 'duplicate with element from same OR
                        'list collection of duplicate elements. throw exceptions once collecting all items
                        sDuplicateElements = sDuplicateElements & oNode.GetAttribute("Name") & vbLf
                    Else
                        oXMLGroupNode.appendChild(oChild)
                    End If
                Else
                    'duplicate with element from different OR
                    sDuplicateElements = sDuplicateElements & oNode.GetAttribute("Name") & "<also found in OR " & GetTSREquivalentForXMLOR(oDuplicateNode.parentNode.GetAttribute("ref")) & ">" & vbLf
                End If
                oChild = Nothing
            Next
            oRoot.appendChild(oXMLGroupNode)
            If sDuplicateElements <> "" Then
                'found duplicates
                ReDim Preserve asDuplicateElements(UBound(asDuplicateElements) + 1)
                asDuplicateElements(UBound(asDuplicateElements)) = "OR NAME: " & GetTSREquivalentForXMLOR(sXML) & vbLf & " Elements:" & vbLf & sDuplicateElements
            End If
        Next
        If UBound(asDuplicateElements) <> -1 Then
            'found some duplidates. Raise exception
            Throw (New Exception("DUPLICATE ELEMENTS FOUND IN OR:" & vbLf & Join(asDuplicateElements, vbLf)))
        End If
        oCommonORXml.save(QTPConfig.psCommonORXML)
        GenerateCommonORXML = aoORObjects
    End Function

    Public Function UpdateCommonORXML(ByVal sXML As String) 'This is called when we have loading of OR at runtime
        Dim oNodeCollection, oNode, bPageFound, sTempPage
        Dim aoORObjects(-1), bDuplicate
        Dim oCommonORXml : oCommonORXml = CreateObject("Msxml2.DOMDocument.3.0")
        Dim oRoot, oChild, oXMLGroupNode, asDuplicateElements(-1), sDuplicateElements, oDuplicateNode
        oCommonORXml.load(QTPConfig.psCommonORXML) 'Load common OR for update
        oRoot = oCommonORXml.documentElement

        Dim xmldoc As Object
        Dim sNodeName = ""
        sTempPage = ""
        'MsgBox(sXML)
        xmldoc = CreateObject("Msxml2.DOMDocument.3.0")
        xmldoc.setProperty("SelectionLanguage", "XPath")
        xmldoc.setProperty("SelectionNamespaces", "xmlns:qtpRep='http://www.mercury.com/qtp/ObjectRepository'")

        xmldoc.load(sXML)


        xmldoc.async = False
        oNodeCollection = xmldoc.selectNodes("//qtpRep:Object") 'Get the object collection using XPath
        oXMLGroupNode = oCommonORXml.createElement("or")
        oXMLGroupNode.setAttribute("ref", sXML)
        oXMLGroupNode.setAttribute("active", "yes")

        bPageFound = False 'It will be set to true if current object has page in its hierarchy
        'MsgBox(oObjectName)
        sDuplicateElements = ""
        For Each oNode In oNodeCollection
            sNodeName = UCase(Trim(oNode.GetAttribute("Name")))
            oDuplicateNode = oCommonORXml.SelectSingleNode("//or[@active='yes']/element[@elementName='" & sNodeName & "']")
            If oDuplicateNode Is Nothing Then
                'its not duplicate

                oChild = oCommonORXml.createElement("element")
                oChild.setAttribute("elementName", sNodeName)
                bDuplicate = False 'This is set to true if any duplidates in below function call
                oChild.text = GetHierarchy(sXML, oNode.GetAttribute("Name"), bDuplicate)
                If bDuplicate Then 'duplicate with element from same OR
                    'list collection of duplicate elements. throw exceptions once collecting all items
                    sDuplicateElements = sDuplicateElements & oNode.GetAttribute("Name") & vbLf
                Else
                    oXMLGroupNode.appendChild(oChild)
                End If
            Else
                'duplicate with element from different OR
                sDuplicateElements = sDuplicateElements & oNode.GetAttribute("Name") & "<also found in OR " & GetTSREquivalentForXMLOR(oDuplicateNode.parentNode.GetAttribute("ref")) & ">" & vbLf
            End If
            oChild = Nothing
        Next
        oRoot.appendChild(oXMLGroupNode)
        If sDuplicateElements <> "" Then
            'found duplicates
            ReDim Preserve asDuplicateElements(UBound(asDuplicateElements) + 1)
            asDuplicateElements(UBound(asDuplicateElements)) = "OR NAME: " & GetTSREquivalentForXMLOR(sXML) & vbLf & " Elements:" & vbLf & sDuplicateElements
        End If

        If UBound(asDuplicateElements) <> -1 Then
            'found some duplidates. Raise exception
            Throw (New Exception("DUPLICATE ELEMENTS FOUND IN OR:" & vbLf & Join(asDuplicateElements, vbLf)))
        End If
        oCommonORXml.save(QTPConfig.psCommonORXML)
    End Function



    Public Function RemoveFromCommonORXML(ByVal sXML As String) 'This is called when we have loading of OR at runtime
        Dim oCommonORXml : oCommonORXml = CreateObject("Msxml2.DOMDocument.3.0")
        Dim oNode
        oCommonORXml.load(QTPConfig.psCommonORXML) 'Load common OR for update
        oNode = oCommonORXml.SelectSingleNode("//or[@active='yes' And @ref='" & sXML & "' ]")
        If oNode Is Nothing Then oNode.setAttribute("active", "no")
        oCommonORXml.save(QTPConfig.psCommonORXML)
    End Function

    Public Function GetHierarchy(ByVal sXMLPath As String,
                                 ByVal oObjectName As String,
                                 ByRef bDuplicate As Boolean)  '\\Returns  collection of OR objects with the name passed as param
        Dim oNodeCollection, oNode, sHierarchy, bPageFound, sTempPage
        Dim aoORObjects(-1)
        Dim psRootObject, psCurrentPage
        Dim xmldoc As Object
        sTempPage = ""
        xmldoc = CreateObject("Msxml2.DOMDocument.3.0")
        xmldoc.setProperty("SelectionLanguage", "XPath")
        xmldoc.setProperty("SelectionNamespaces", "xmlns:qtpRep='http://www.mercury.com/qtp/ObjectRepository'") 'setting up namespaces
        xmldoc.load(sXMLPath)
        xmldoc.async = False
        'Ignoring case sencitivity while looking for elements buy translating to upper case
        oNodeCollection = xmldoc.selectNodes("//qtpRep:Object[translate(@Name, 'abcdefghijklmnopqrstuvwxyz', 'ABCDEFGHIJKLMNOPQRSTUVWXYZ')='" & UCase(Trim(oObjectName)) & "']") 'Get the object collection using XPath
        If oNodeCollection.length > 1 Then
            bDuplicate = True

            GetHierarchy = vbEmpty
            Exit Function 'we dont need to continue parsing for hierarchy since element is duplidate
        End If

        bPageFound = False 'It will be set to true if current object has page in its hierarchy
        For Each oNode In oNodeCollection 'I know it would have single element if it has come till this point ;) Yeah, need to clean up.
            sHierarchy = ""
            Do
                If (oNode.GetAttribute("Class").ToString() <> "") Then 'And oNode.GetAttribute("Class") IsNot "" Then
                    sHierarchy = oNode.GetAttribute("Class") & "(""" & oNode.GetAttribute("Name") & """)" & "." & sHierarchy   'Build the object hierarchy
                    sTempPage = oNode.GetAttribute("Class") & "(""" & oNode.GetAttribute("Name") & """)" & "." & sTempPage 'Build the object hierarchy
                End If
                If LCase(Trim(oNode.GetAttribute("Class").ToString())) = "browser" Then
                    psRootObject = oNode.GetAttribute("Class") & "(""" & oNode.GetAttribute("Name") & """)"
                    Exit Do
                ElseIf LCase(Trim(oNode.GetAttribute("Class").ToString())) = "page" Then
                    oNode = oNode.parentNode
                    bPageFound = True
                Else
                    oNode = oNode.parentNode
                    If oNode.GetAttribute("Name").ToString() <> "" Then
                        psRootObject = oNode.GetAttribute("Class") & "(""" & oNode.GetAttribute("Name") & """)"
                        If Not bPageFound Then sTempPage = ""
                    End If
                End If
            Loop Until oNode.nodeName = "qtpRep:Objects"
            sHierarchy = Trim(sHierarchy)
            sHierarchy = Left(sHierarchy, Len(sHierarchy) - 1)
            If bPageFound Then 'If page is not found, it will have previously used page as current page
                sTempPage = Trim(sTempPage)
                psCurrentPage = Left(sTempPage, Len(sTempPage) - 1)
            End If
            ReDim Preserve aoORObjects(UBound(aoORObjects) + 1)
            aoORObjects(UBound(aoORObjects)) = sHierarchy 'Evaluate the object hierarchy string gives the object
        Next
        GetHierarchy = aoORObjects(0)
    End Function
    '*********************************************************************************************************************************
    'Function Name  :    RecursiveFilter
    'Purpose        :	 Filter the step data
    'Parameteres    :	String
    'Author         : 	  Pradeep Buddaraju
    'Date			:	  
    '*********************************************************************************************************************************
    Function RecursiveFilter(ByVal sData, ByVal sFilter, ByVal sReplaceWith)
        Do
            sData = Replace(sData, sFilter, sReplaceWith, 1, -1, 1)
        Loop Until InStr(1, sData, sFilter, 1) = 0
        RecursiveFilter = Trim(sData)
    End Function
    '*********************************************************************************************************************************
    'Function Name      :    GetTestDataObjIndex
    'Purpose            :	 Get the object index in Test data sheet
    'Parameteres		:	String
    'Author				: 	  Pradeep Buddaraju
    'Date				:	  06-Mar-2012
    '*********************************************************************************************************************************
    Function GetTestDataObjIndex(ByVal sSheet)
        Dim nIndex

        'MsgBox("GetTestDataObjIndex")
        Dim oEnv
        oEnv = CreateObject("com.ags.aft.Engine.QTP.Configuration")
        sSheet = LCase(Trim(sSheet))
        For nIndex = 0 To UBound(Core.psTestDataSheets)
            If sSheet = Core.psTestDataSheets(nIndex) Then Exit For
        Next
        'MsgBox("INDEX:" & nIndex & " UBound(psTestDataSheets): " & UBound(psTestDataSheets))
        If nIndex > UBound(Core.psTestDataSheets) Then 'Sheet is not loaded yet. So load it!
            ReDim Preserve Core.psTestDataSheets(UBound(Core.psTestDataSheets) + 1)
            ReDim Preserve Core.paoTestData(UBound(Core.paoTestData) + 1)
            'MsgBox("UBound(paoTestData)" & UBound(paoTestData) & " UBound(psTestDataSheets) " & UBound(psTestDataSheets))
            Core.psTestDataSheets(UBound(Core.psTestDataSheets)) = sSheet
            Core.paoTestData(UBound(Core.paoTestData)) = CreateObject("com.ags.aft.Engine.QTP.Excel")
            Core.paoTestData(UBound(Core.paoTestData)).Load(oEnv.Environment("TEST_DATA_PATH"), sSheet, 1)
            GetTestDataObjIndex = UBound(Core.paoTestData)
            'MsgBox(UBound(paoTestData))
            'MsgBox(paoTestData(UBound(paoTestData)).GetFileName & "index" & nIndex)
            'Else
            '    GetTestDataObjIndex = -1
        End If
    End Function


    Function FilterStatement(ByVal sData)
        Dim nStart, nEnd, sSheet, sName, sVar, sCondition
        Dim sOperators, nPosition, sChars, sLiteral, nLengthBeforeConversion
        'asOperators = Array("+", "-","=", ">",">=","<", "<=","/","*", "%", "<>", "^")
        sOperators = ",+,-,=,>,>=,<,<=,/,*,%,<>,^,(,),"

        'Filter variables
        Do While (InStr(sData, "#") <> 0) 'Extract all variables from the list and rebuild the statement
            nStart = InStr(sData, "#")
            nEnd = InStr(nStart + 1, sData, "#")
            sVar = Trim(Mid(sData, nStart, nEnd - nStart + 1))
            sVar = Trim(RecursiveFilter(sVar, "#", ""))
            sVar = Trim(RecursiveFilter(sVar, "  ", " "))
            sVar = UCase(Trim(RecursiveFilter(sVar, " ", "_")))
            '		sData = Left(sData, nStart-1) & " Environment(""VAR_" & sVar &""") " & Right(sData, Len(sData)-nEnd)
            sData = Left(sData, nStart - 1) & " oEnv.Environment(""VAR_" & sVar & """) " & Right(sData, Len(sData) - nEnd)
        Loop

        'Filter with test data
        While (Not (InStr(sData, "${") = 0 And InStr(sData, "}$") = 0))
            nStart = InStr(sData, "${")
            nEnd = InStr(nStart + 1, sData, "}$") + 1
            sVar = Trim(Mid(sData, nStart, nEnd - nStart + 1))
            sSheet = Mid(sVar, 3, InStr(sVar, ".") - 3)
            sSheet = RecursiveFilter(sSheet, "'", "")
            sName = Mid(sVar, InStr(sVar, ".") + 1)
            sName = Left(sName, InStr(sName, "}") - 1)
            sName = RecursiveFilter(sName, "'", "")
            sVar = paoTestData(GetTestDataObjIndex(sSheet)).GetTestData(sName)
            sData = Trim(Left(sData, nStart - 1) & sVar & Right(sData, Len(sData) - nEnd))
        End While

        If InStr(sData, "(") > 0 And InStr(sData, ")") > 0 And InStr(sData, "?") > 0 Then
            sCondition = Left(sData, InStr(sData, "?") - 1) 'sCondition 
            'typecast all string literals
            nPosition = 1
            nStart = 0
            nEnd = 0
            Do
                sChars = Mid(sCondition, nPosition, 1)
                If nStart = 0 Then nStart = nPosition
                If InStr(sOperators, "," & sChars & ",") > 0 Then
                    'Found operartor
                    nEnd = nPosition - 1
                    If nStart < nEnd Then
                        sLiteral = Mid(sCondition, nStart, nEnd - nStart + 1)
                        nLengthBeforeConversion = Len(sLiteral)
                        If Not IsNumeric(sLiteral) Then
                            If Left(sLiteral, 1) <> """" And Right(sLiteral, 1) <> """" Then
                                sLiteral = """" & LCase(Trim(sLiteral)) & """"
                                'sTemp = sCondition 
                                nPosition = nPosition + nLengthBeforeConversion - Len(sLiteral) 'set position correctly after conversion
                                sCondition = Left(sCondition, nStart - 1) & sLiteral & Mid(sCondition, nEnd + 1)
                            End If
                        End If
                        nStart = 0
                        nEnd = 0
                    Else
                        nStart = 0
                    End If
                    'check if its combination on operators
                    If InStr(sOperators, "," & Mid(sCondition, nPosition, 2) & ",") > 0 Then nPosition = nPosition + 1

                End If
                nPosition = nPosition + 1
                If nPosition > Len(sCondition) Then Exit Do
            Loop

            If Commands.CustomEVal(sCondition) Then
                MsgBox(Mid(sData, InStr(sData, "?") + 1, InStr(sData, ":") - InStr(sData, "?") - 1))
            Else
                MsgBox(Mid(sData, InStr(sData, ":") + 1))
            End If
        End If
        FilterStatement = sData
    End Function

    Function GetORObject(ByVal sXML, ByVal sObjectName)
        Dim sNodeName
        Dim xmldoc
        sNodeName = ""
        xmldoc = CreateObject("Msxml2.DOMDocument.3.0")
        xmldoc.setProperty("SelectionLanguage", "XPath")
        xmldoc.load(sXML)
        xmldoc.async = False
        GetORObject = GetHierarchy(xmldoc, sObjectName)
        xmldoc = Nothing
    End Function

    Function getHierarchy(ByVal xmldoc, ByVal sObjectName)
        Dim oNode, sParent

        oNode = xmldoc.selectSingleNode("//Element[@name=""" + sObjectName + """]/id")
        sParent = oNode.parentNode.GetAttribute("parent")
        If sParent <> "" Then
            getHierarchy = getHierarchy(xmldoc, sParent) & "." & oNode.text
        Else
            getHierarchy = oNode.text
        End If
    End Function

End Class
