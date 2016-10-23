
Public Class SerializeTable
    Dim saveFile, oFile, serializedObjectsPath
    Dim _ToolProxyObject As Object
    Sub New()
        serializedObjectsPath = System.IO.Path.GetTempPath() & "\" & "xAFT_Temp"
        Dim ofso
        ofso = CreateObject("Scripting.FileSystemObject")
        If Not ofso.FolderExists(serializedObjectsPath) Then
            ofso.CreateFolder(serializedObjectsPath)
        End If
        ofso = Nothing
    End Sub

    Sub setPath(ByVal sSerializeObjectsPath)
        serializedObjectsPath = sSerializeObjectsPath
    End Sub

    Sub setToolProxyObject(ByVal oObject)
        _ToolProxyObject = oObject
    End Sub

    Function getTable(ByVal sTableName)
        saveFile = CreateObject("Scripting.FileSystemObject")


        oFile = saveFile.OpenTextFile(serializedObjectsPath & "\\xAFT_" & sTableName, 1)
        Dim headers(-1) As String, row(-1) As String
        Dim tableRows()() As String

        headers = oFile.ReadLine().ToString().Split(";")
        ReDim tableRows(-1)
        ReDim Preserve headers(UBound(headers) - 1)

        Do Until oFile.AtEndOfStream
            ReDim Preserve tableRows(UBound(tableRows) + 1)
            ReDim row(-1)
            row = oFile.ReadLine().ToString().Split("|#|")
            ReDim Preserve row(UBound(row) - 1)
            tableRows(UBound(tableRows)) = row
        Loop
        getTable = New Table
        getTable.setRows(tableRows)
        getTable.setHeaders(headers)

    End Function

    Sub saveTable(ByVal oTable As Object, ByVal sTableName As String)
        _ToolProxyObject.setObject(oTable)
        Dim cols, rowData
        saveFile = CreateObject("Scripting.FileSystemObject")

        On Error Resume Next
        oFile = saveFile.CreateTextFile(serializedObjectsPath & "\\xAFT_" & sTableName, True)
        oFile.writeLine(_ToolProxyObject.getROProperty("column_names").ToString())
        If Err.Number <> 0 Then
            oFile.close()
            oFile = Nothing
            saveFile = Nothing
            Throw New Exception("saveTable " & Err.Description)

        End If
        cols = _ToolProxyObject.getROProperty("cols")

        For n = 0 To _ToolProxyObject.getROProperty("rows") - 1
            rowData = ""
            For col = 0 To cols - 1
                rowData = rowData & _ToolProxyObject.getCellData(n, col) & "|#|"
                If Err.Number <> 0 Then
                    oFile.close()
                    oFile = Nothing
                    saveFile = Nothing
                    Throw New Exception("saveTable " & Err.Description)
                End If
            Next
            oFile.writeLine(rowData)
        Next
        On Error GoTo 0
        oFile.close()
        oFile = Nothing
        saveFile = Nothing
    End Sub

End Class

<Serializable()> Public Class Table
    Public tableRows()() As String
    Public tableHeaders(-1) As String


    Sub New()
        ReDim tableRows(-1)
        ReDim tableHeaders(-1)
    End Sub

    Sub New(ByVal nRows, ByVal nCols)
        nRows = CInt(nRows)
        nCols = CInt(nCols)

        ReDim tableRows(-1)
        ReDim tableHeaders(nCols - 1)
    End Sub

    Sub setRows(ByVal rows As String()())
        tableRows = rows
    End Sub

    Sub setHeaders(ByVal asHeadersArray)
        tableHeaders = asHeadersArray
    End Sub
    Function setDataByRow(ByVal dataArrayByColumn)
        ReDim Preserve tableRows(UBound(tableRows) + 1)

        tableRows(UBound(tableRows)) = dataArrayByColumn
    End Function

    Function getHeaders()
        getHeaders = String.Join(";", tableHeaders) & ";"
    End Function

    Function getRowCount()
        getRowCount = UBound(tableRows) + 1
    End Function

    Function getColumnCount()
        getColumnCount = UBound(tableHeaders) + 1
    End Function

    Function getTableArray()
        getTableArray = tableRows
    End Function

    Private Function getColIndex(ByVal sColumnName)
        Dim i As Integer

        For i = 0 To UBound(tableHeaders)
            If LCase(Trim(tableHeaders(i))) = LCase(Trim(sColumnName)) Then
                getColIndex = i
                Exit For
            End If
        Next
        If i > UBound(tableHeaders) Then getColIndex = -1
    End Function

    Function getDataByColumn(ByVal sColumnName, ByVal sColumnValue, ByVal retColumn)
        Dim index As Integer
        Dim row As Integer

        index = getColIndex(sColumnName)
        If index = -1 Then
            Throw New Exception("Get Column index based on column name: Unknown column- " & sColumnName)
        Else
            On Error Resume Next
            For row = 0 To UBound(tableRows)
                If LCase(Trim(tableRows(row)(index))) = LCase(Trim(sColumnValue)) Then
                    getDataByColumn = tableRows(row)(getColIndex(retColumn))
                End If
            Next
            If Err.Number > 0 Then
                Throw New Exception("Exception while retreiving data by column GetDataByColumn " & Err.Description)
            End If
            On Error GoTo 0
            If row > UBound(tableRows) Then
                Throw New Exception("Couldnt find data by column GetDataByColumn " & sColumnName & "-" & sColumnValue & " ret Column:" & retColumn)
            End If
        End If
    End Function

    Function getDataByColumnIndex(ByVal sColumnNameIndex, ByVal sColumnValue, ByVal retColumnIndex)
        Dim index As Integer
        Dim row As Integer

        index = CInt(sColumnNameIndex)
        retColumnIndex = CInt(retColumnIndex)
        If index = -1 Then
            Throw New Exception("Get Column index based on column name: Unknown column- " & sColumnNameIndex)
        Else
            On Error Resume Next
            For row = 0 To UBound(tableRows)
                If LCase(Trim(tableRows(row)(index))) = LCase(Trim(sColumnValue)) Then
                    getDataByColumnIndex = tableRows(row)(retColumnIndex)
                End If
            Next
            If Err.Number > 0 Then
                Throw New Exception("Exception while retreiving data by column GetDataByColumn " & Err.Description)
            End If
            On Error GoTo 0
            If row > UBound(tableRows) Then
                Throw New Exception("Couldnt find data by column GetDataByColumn " & sColumnNameIndex & "-" & sColumnValue & " ret Column:" & retColumnIndex)
            End If
        End If
    End Function
End Class


