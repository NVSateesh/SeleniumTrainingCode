Imports System.IO
Imports System.Runtime.Serialization.Formatters.Binary

Public Class SerializeObjects
    Private defaultPath As String

    Sub New()

    End Sub
    
    Sub New(ByVal sPath)
        defaultPath = sPath
    End Sub

    Sub setPath(ByVal sPath)
        defaultPath = sPath

    End Sub

    Public Function saveTable(ByVal oObject As Object, ByVal sName As String)
        Dim oTable, oTable1 As Table
        Dim columnHeaders(-1) As String

        ReDim columnHeaders(-1)
        columnHeaders = oObject.getROProperty("Columns_Names").ToString().Split(";")
        oTable = New Table(oObject.getROProperty("rows"), oObject.getROProperty("cols"))

        ReDim Preserve columnHeaders(UBound(columnHeaders) - 1)

        MsgBox("0")
        oTable.setHeaders(columnHeaders)
        For n = 0 To oObject.getROProperty("rows") - 1
            For col = 0 To UBound(columnHeaders)
                columnHeaders(col) = oObject.getCellData(n, col)
            Next
            oTable.setDataByRow(columnHeaders)
        Next
        MsgBox("1")

        Dim mst As Stream = New MemoryStream()
        Dim fs As Stream = New FileStream(defaultPath & "\\" & sName, FileMode.Create)
        Dim bf As BinaryFormatter = New BinaryFormatter()
        bf.Serialize(mst, oTable)
        oObject = bf.Deserialize(mst)
        mst.Flush()

        MsgBox("a1")
        oTable1 = CType(oObject, Table)


        fs.Close()
    End Function

    Public Function getTable(ByVal sName) As Table
        MsgBox(defaultPath & "\\" & sName)
        Dim fs1 As Stream = New FileStream(defaultPath & "\\" & sName, FileMode.Open)
        Dim bf As BinaryFormatter = New BinaryFormatter()


        Dim oTable As Table
        MsgBox("a")
        Dim oObject As Table
        oObject = bf.Deserialize(fs1)
        MsgBox("a1")
        oTable = CType(oObject, Table)
        MsgBox("b")
        fs1.Close()
        getTable = oTable
    End Function

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

        ReDim tableRows(nRows - 1)
        ReDim tableHeaders(nCols - 1)
    End Sub

    Function setDataByRow(ByVal dataArrayByColumn)
        ReDim Preserve tableRows(UBound(tableRows) + 1)

        tableRows(UBound(tableRows)) = dataArrayByColumn
    End Function

    Sub setHeaders(ByVal asHeadersArray)
        tableHeaders = asHeadersArray
    End Sub

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


