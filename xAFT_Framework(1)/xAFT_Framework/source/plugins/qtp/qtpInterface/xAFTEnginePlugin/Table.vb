
Namespace table

    <Serializable()> Public Class Table
        Public tableRows()() As String
        Public tableHeaders(-1) As String


        Sub New()
            ReDim tableRows(-1)
            ReDim tableHeaders(-1)
        End Sub

        Function name()
            name = "xAFTTable"
        End Function

        Sub New(ByVal oTableObject As Object)
            setTable(oTableObject)
        End Sub

        Sub setTable(ByVal oTableObject As Object)
            Dim headers(-1) As String, row(-1) As String
            Dim tableRows()() As String

            On Error Resume Next
            Dim objName = oTableObject.name
            If Err.Number = 0 Then 'xAFT Table is passed 
                tableRows = oTableObject.tableRows
                tableHeaders = oTableObject.tableHeaders
                Exit Sub
            End If
            On Error GoTo 0
            CommonConfig._ProxyToolObject.setObject(oTableObject)
            If String.IsNullOrEmpty(CommonConfig._ProxyToolObject.getProperty("columns_names")) Then
                headers = CommonConfig._ProxyToolObject.getProperty("columns_names").ToString().Split(";")
            Else
                headers = CommonConfig._ProxyToolObject.getProperty("columns_names").ToString().Split(";")

            End If

            
            ReDim tableRows(-1)
            ReDim Preserve headers(UBound(headers) - 1)
            Dim nCols
            nCols = CommonConfig._ProxyToolObject.getProperty("cols")
            For nRow = 0 To CommonConfig._ProxyToolObject.getProperty("rows") - 1

                ReDim Preserve tableRows(UBound(tableRows) + 1)
                ReDim row(nCols - 1)
                For nCol = 0 To nCols - 1
                    row(nCol) = Trim(CommonConfig._ProxyToolObject.getCellData(nRow, nCol))
                Next
                tableRows(UBound(tableRows)) = row
            Next
            setRows(tableRows)
            setHeaders(headers)
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
        Sub setDataByRow(ByVal dataArrayByColumn)
            ReDim Preserve tableRows(UBound(tableRows) + 1)

            tableRows(UBound(tableRows)) = dataArrayByColumn
        End Sub

        Function getHeaders()
            getHeaders = String.Join(";", tableHeaders) & ";"
        End Function

        Function getRowCount()
            getRowCount = UBound(tableRows) + 1
        End Function

        Function getColumnCount()
            getColumnCount = UBound(tableHeaders) + 1
        End Function

        Function getRowData(ByVal nRow)
            getRowData = Join(tableRows(nRow), "|#|")
        End Function
        Function getTableArray() As String()()

            getTableArray = tableRows
        End Function

        Function getCellData(ByVal nRow, ByVal nCol)
            getCellData = tableRows(nRow)(nCol)
        End Function

        Private Function getColIndex(ByVal sColumnName)
            Dim i As Integer
            getColIndex = -1
            For i = 0 To UBound(tableHeaders)
                If LCase(Trim(tableHeaders(i))) = LCase(Trim(sColumnName)) Then
                    getColIndex = i
                    Exit For
                End If
            Next
            'If i > UBound(tableHeaders) Then getColIndex = -1 'no need for this condition as we are initializing this to -1 
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

    '*********************************************************************************************************************************
    'Class Name         :    SerializeTable, Table
    'Purpose            :	 To serialize runtime table objects so we can use them later for validations 
    '                        by deserializing to 'Table' class instance. Since we need to access tool based libraries to access 
    '                        runtime object, a proxy object class is created with in tool and its instance is set to _ToolProxyObject.
    '                        This is specifically used for QTP and will be modified to hold other tool based tables. 
    'Parameteres		:	
    'Author				: 	  Pradeep Buddaraju
    'Date				:	  Dec-2013
    '*********************************************************************************************************************************
    Public Class SerializeTable
        Dim saveFile, oFile, serializedObjectsPath
        'CommonConfig._ToolProxyObject holds proxy object class from QTP so that we can access QTP libraries from this assembly at runtime.
        Sub New()
            serializedObjectsPath = System.IO.Path.GetTempPath() & "\" & "xAFT_Temp" 'serialized objects path in temp folder.
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


        Function getTable(ByVal sTableName) 'parse serialized object and return table kind of object for easy access to data and validations
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

        'custom serialization for table objects to configured folder. 
        '.net serialization had some issues with deserialization when the assemble is registered as com. 
        'The object type passed to COM is not visible to assembly at runtime. 
        Sub saveTable(ByVal oTable As Object, ByVal sTableName As String)
            CommonConfig._ProxyToolObject.setObject(oTable)
            Dim cols, rowData
            saveFile = CreateObject("Scripting.FileSystemObject")

            On Error Resume Next
            oFile = saveFile.CreateTextFile(serializedObjectsPath & "\\xAFT_" & sTableName, True)
            oFile.writeLine(CommonConfig._ProxyToolObject.getProperty("columns_names").ToString())
            If Err.Number <> 0 Then
                oFile.close()
                oFile = Nothing
                saveFile = Nothing
                Throw New Exception("saveTable " & Err.Description)

            End If
            cols = CommonConfig._ProxyToolObject.getProperty("cols")

            For n = 0 To CommonConfig._ProxyToolObject.getProperty("rows") - 1
                rowData = ""
                For col = 0 To cols - 1
                    rowData = rowData & CommonConfig._ProxyToolObject.getCellData(n, col) & "|#|"
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



    Public Class TableCompare
        Dim oTable1 As Table, oTable2 As Table
        Dim ExcludeRows As Integer() 'to store rows which are already verified.

        Dim Tab1_nRows As Integer = 0
        Dim Tab1_nCols As Integer = 0
        Dim Tab2_nRows As Integer = 0
        Dim Tab2_nCols As Integer = 0
        Dim Tab1_Data()() As String, Tab2_Data()() As String, Tab1_Headers, Tab2_Headers

        Sub New()

        End Sub

        Sub New(ByVal oTable1, ByVal oTable2)
            oTable1 = oTable1
            oTable2 = oTable2
        End Sub


        Sub setTables(ByVal tab1 As String, ByVal tab2 As String)
            oTable1 = DataStore.TableStore(tab1)
            oTable2 = DataStore.TableStore(tab2)
        End Sub

        Sub setDefaultToolProxyObject()
            CommonConfig._ProxyToolObject = New DefaultToolProxyObject

        End Sub

        Sub setToolProxyObject(ByVal oObject)
            CommonConfig._ProxyToolObject = oObject
        End Sub

        Function areExactMatch()

            If oTable1.getRowCount <> oTable2.getRowCount Then
                areExactMatch = False
                Exit Function
            End If
            'compare columns
            If oTable1.getRowCount > 0 Then

                If oTable1.getColumnCount <> oTable2.getColumnCount Then
                    areExactMatch = False
                    Exit Function
                End If
            End If
            'compare table headers
            If oTable1.getHeaders <> oTable2.getHeaders Then
                areExactMatch = False
                Exit Function
            End If
            'Compare data as rows and columns counts are same
            For nRow = 0 To oTable1.getRowCount - 1
                If oTable1.getRowData(nRow) <> oTable2.getRowData(nRow) Then
                    areExactMatch = False
                    Exit Function
                End If
            Next
            areExactMatch = True
        End Function

        Function getRowIndexFromTable2(ByVal nRow)
            Dim table1RowData As String = oTable1.getRowData(nRow)
            getRowIndexFromTable2 = -1
            For nRow = 0 To oTable2.getRowCount - 1
                If table1RowData = oTable1.getRowData(nRow) Then
                    getRowIndexFromTable2 = nRow
                    Exit Function
                End If
            Next
        End Function

        Function Table2ContainsTable1()
            Dim nRows As Integer = 0
            Dim nCols As Integer = 0




            Table2ContainsTable1 = False
            nRows = oTable2.getRowCount

            'compare columns
            If nRows > 0 Then
                nCols = oTable1.getColumnCount()
                If nCols <> oTable2.getColumnCount() Then
                    Table2ContainsTable1 = False
                    Exit Function
                End If
            End If

            'compare table headers
            If oTable1.getHeaders() <> oTable2.getHeaders() Then
                Table2ContainsTable1 = False
                Exit Function
            End If

            'Compare data 
            For nRow = 0 To oTable1.getRowCount - 1
                If getRowIndexFromTable2(nRow) - 1 Then
                    Table2ContainsTable1 = False
                    Exit Function
                End If
            Next
        End Function
    End Class

    Class DefaultToolProxyObject
        Dim oObject As Object

        Sub setObject(ByVal oQTPObject)
            oObject = oQTPObject
        End Sub

        Function GetProperty(ByVal sProperty)
            GetProperty = oObject.getROProperty(sProperty)
        End Function

        Function GetCellData(ByVal nRow, ByVal nCol)
            GetCellData = oObject.GetCellData(nRow, nCol)
        End Function
    End Class

    Public Class DataStore
        Public Shared TableStore As Dictionary(Of String, Table)
        Sub New()
            TableStore = New Dictionary(Of String, Table)
        End Sub

        Public Sub Push(ByVal sName As String, ByVal oTable As Object)

            TableStore.Add(sName, New Table(oTable))
        End Sub

        Public Sub Remove(ByVal sName)
            TableStore.Remove(sName)
        End Sub

        Public Sub Clear()
            TableStore.Clear()
        End Sub
    End Class

End Namespace