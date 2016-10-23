Friend Class CommonConfig
    Public Shared executionEngine As String
    Public Shared isCentral As Boolean


    '****This holds tool specific object. This would have access to tool specific libraries and 
    'this enables wraper libraries for plugin to access tool specifc libraries****
    Public Shared _ToolInstance As Object
    Public Shared _ActionFixture As String
    Public Shared _ListenerPort As String
    Public Shared _rootPath As String

    Public Shared _ProxyToolObject As Object 'object that tool understands
    'TODO: need to move all the common configuration and reduce dependency of creating tool specifc config and methods.

    Public Sub setActionFixturePath(ByVal sPath As String) 'to be able to validate independently from com with out tool
        _ActionFixture = sPath
    End Sub

  


    Public Shared Function getActionCommand(ByVal xAFTObjectType, ByVal sToolObjectType, ByVal xAFTAction, ByVal sActionData)
        Dim oNode, sActionName, sParameterDelimiter, sDataParams(-1), oChildNodes

        'MsgBox("xAFTObjectType, ByVal sToolObjectType, ByVal xAFTAction, ByVal sActionData:" & xAFTObjectType & "," & sToolObjectType & "," & xAFTAction & "," & sActionData)
        Dim aoORObjects(-1) ', sXML

        Dim xmldoc As Object
        xmldoc = CreateObject("Msxml2.DOMDocument.3.0")
        xmldoc.setProperty("SelectionLanguage", "XPath")
        xmldoc.load(CommonConfig._ActionFixture)
        If isCentral Then
            oNode = xmldoc.SelectSingleNode("//object[translate(@xAFTObject, 'abcdefghijklmnopqrstuvwxyz', 'ABCDEFGHIJKLMNOPQRSTUVWXYZ')='" _
                                            & UCase(Trim(xAFTObjectType)) & "']" _
                                            & "/action[" _
                                            & "translate(@xAFTAction, 'abcdefghijklmnopqrstuvwxyz', 'ABCDEFGHIJKLMNOPQRSTUVWXYZ')='" _
                                            & UCase(Trim(xAFTAction)) & "' and" _
                                            & " contains(translate(@toolObject, 'abcdefghijklmnopqrstuvwxyz', 'ABCDEFGHIJKLMNOPQRSTUVWXYZ'),'" _
                                            & UCase(Trim(sToolObjectType)) & "')]") 'Get the object collection using XPath
        Else

            oNode = xmldoc.SelectSingleNode("//action[" _
                                            & "translate(@xAFTAction, 'abcdefghijklmnopqrstuvwxyz', 'ABCDEFGHIJKLMNOPQRSTUVWXYZ')='" _
                                            & UCase(Trim(xAFTAction)) & "' and" _
                                            & " contains(translate(@toolObject, 'abcdefghijklmnopqrstuvwxyz', 'ABCDEFGHIJKLMNOPQRSTUVWXYZ'),'" _
                                            & UCase(Trim(sToolObjectType)) & "')]") 'Get the object collection using XPath
        End If
        
        If oNode Is Nothing Then
            getActionCommand = ""
            Exit Function
        End If
        'Find number of params
        sActionName = oNode.GetAttribute("toolAction")
        sParameterDelimiter = oNode.GetAttribute("parameterDelimiter")
        sDataParams = sActionData.ToString().Split(sParameterDelimiter)
        'Parameters from Data
        If Trim(sActionData) <> "" Then

            If isCentral Then
                oChildNodes = xmldoc.SelectNodes("//object[translate(@xAFTObject, 'abcdefghijklmnopqrstuvwxyz', 'ABCDEFGHIJKLMNOPQRSTUVWXYZ')='" _
                                        & UCase(Trim(xAFTObjectType)) & "']" _
                                        & "/action[" _
                                        & "translate(@xAFTAction, 'abcdefghijklmnopqrstuvwxyz', 'ABCDEFGHIJKLMNOPQRSTUVWXYZ')='" _
                                        & UCase(Trim(xAFTAction)) & "' and" _
                                        & " contains(translate(@toolObject, 'abcdefghijklmnopqrstuvwxyz', 'ABCDEFGHIJKLMNOPQRSTUVWXYZ'),'" _
                                        & UCase(Trim(sToolObjectType)) & "')]" _
                                         & "/parameter")
            Else

                oChildNodes = xmldoc.SelectNodes("//action[" _
                                        & "translate(@xAFTAction, 'abcdefghijklmnopqrstuvwxyz', 'ABCDEFGHIJKLMNOPQRSTUVWXYZ')='" _
                                        & UCase(Trim(xAFTAction)) & "' and" _
                                        & " contains(translate(@toolObject, 'abcdefghijklmnopqrstuvwxyz', 'ABCDEFGHIJKLMNOPQRSTUVWXYZ'),'" _
                                        & UCase(Trim(sToolObjectType)) & "')]" _
                                         & "/parameter")
            End If

            Dim sParamData = ""
            Dim isMandatory As String
            For i = 0 To oChildNodes.length - 1
                isMandatory = LCase(Trim(oChildNodes.Item(i).getAttribute("mandatory")))
                On Error Resume Next
                sParamData = Trim(sDataParams(i))

                If Err.Number <> 0 And isMandatory = "yes" Then
                    'no data, but params are available
                    ReDim Preserve sDataParams(UBound(sDataParams) + 1)
                ElseIf Err.Number <> 0 And isMandatory <> "yes" Then
                    Exit For
                End If
                If Trim(sDataParams(i)) = "" And isMandatory = "yes" Then
                    sDataParams(i) = oChildNodes.Item(i).getAttribute("default")
                End If
                If LCase(Trim(oChildNodes.Item(i).getAttribute("datatype"))) = "string" Then
                    sDataParams(i) = """" & sDataParams(i) & """"
                End If
                On Error GoTo 0
            Next
        End If
        getActionCommand = sActionName & "(" & Join(sDataParams, ",") & ")"
    End Function
End Class

