Imports System.Runtime.InteropServices
Imports System.Threading
Imports System.IO
Imports System.Net
Imports System.Net.Sockets
Imports System.Configuration
Imports System.Text

Friend Class Commands
    
    Public Sub New()
        '//Dummy
    End Sub
   
    Public Shared Function Run(ByVal Cmd, ByRef returnVal, ByRef ErrMessage)
        Select Case LCase(Trim(CommonConfig.executionEngine))
            Case "qtp"
                Run = CommonConfig._ToolInstance.Run(Cmd)
            Case "testcomplete"
                Run = CommonConfig._ToolInstance.Run(Cmd)
            Case Else
                'defaut to qtp
                Run = CommonConfig._ToolInstance.Run(Cmd)
        End Select

        returnVal = Left(Run, InStr(Run, "||") - 1)
        ErrMessage = Mid(Run, InStr(Run, "||") + 2)
        If ErrMessage = "" Then
            Run = True
        Else
            Run = False
        End If
    End Function

    Shared Function CustomEVal(ByVal sData)

        If InStr(1, sData, "oEnv.Environment(""", 1) > 0 Then
            CustomEVal = CommonConfig._ToolInstance.Evaluate(sData)
        Else
            CustomEVal = Trim(sData)
        End If
    End Function

    Public Shared Sub StartListener(ByVal port)
        Dim listener As TcpListener
        'Dim soc As System.Net.Sockets.Socket
        Dim errMsg As String = ""
        Dim ret As String = ""

        'MsgBox("Before opening port")
        'Dim t As Thread
        listener = New TcpListener(System.Net.IPAddress.Parse("127.0.0.1"), CInt(port))
        listener.Start()
        't = New Thread(New ThreadStart(AddressOf Service))
        Dim bytes(1024) As Byte
        Dim data As String = Nothing

        Do

            data = Nothing
            Dim client As TcpClient = listener.AcceptTcpClient()
            ' Get a stream object for reading and writing
            Dim stream As NetworkStream = client.GetStream()

            Dim i As Int32
            Dim msg As Byte()


            ' Loop to receive all the data sent by the client.
            i = stream.Read(bytes, 0, bytes.Length)
            'MsgBox("1")

            While (i <> 0)
                ' Translate data bytes to a ASCII string.
                data = System.Text.Encoding.ASCII.GetString(bytes, 0, i)
                If LCase(Trim(data)) = "stoptest" Then
                    'MsgBox("Stopping Test")
                    msg = System.Text.Encoding.ASCII.GetBytes("||SUCCESS")
                    stream.Write(msg, 0, msg.Length)
                    Exit Do
                End If
                Run(data, ret, errMsg)
                If Trim(errMsg) = "" Then errMsg = "SUCCESS"
                msg = System.Text.Encoding.ASCII.GetBytes(ret & "||" & errMsg)
                ' Send back a response.
                stream.Write(msg, 0, msg.Length)
                stream.Flush()
                i = stream.Read(bytes, 0, bytes.Length)
                'MsgBox("3")
            End While

            ' Shutdown and end connection
            client.Close()
            'MsgBox("4")
            client = Nothing
            stream = Nothing
        Loop While True

    End Sub

    Public Shared Function resolvePath(ByVal sPath)
        If Left(Trim(sPath), 2) = "./" Or Left(Trim(sPath), 2) = ".\" Then
            sPath = CommonConfig._rootPath & "/" & Mid(Trim(sPath), 3)
            sPath = Replace(sPath, "/", "\")
        End If
        Dim oFSO As Object = CreateObject("Scripting.FileSystemObject")
        Dim tmpPath As String

        'Check if this a folder
        On Error Resume Next
        tmpPath = oFSO.GetFolder(sPath).Path
        If Err.Number <> 0 Then
            'Could be a File
            tmpPath = oFSO.GetFile(sPath).Path
        End If
        On Error GoTo 0
        If Trim(tmpPath) <> "" Then
            resolvePath = tmpPath
        Else
            resolvePath = sPath
        End If

        oFSO = Nothing
    End Function

    Public Shared Sub SyncListner()
        'Since external world doenst have control over starting of listner 
        'use this method to sync up till listener is started
        Dim oTcpClient As TcpClient
        oTcpClient = New TcpClient()
        Do
            Try
                oTcpClient.Connect("127.0.0.1", CommonConfig._ListenerPort)
                Exit Do
            Catch ex As Exception
                'ignore exception ;) coz, u need to wait till listener is started
                Thread.Sleep(500)
            End Try
        Loop
        oTcpClient.Close()
        oTcpClient = Nothing
    End Sub

    Public Sub StopQTP()
        Dim ret, errMsg
        ret = ""
        errMsg = ""
        If Not runCommand("stoptest", ret, errMsg) Then
            Throw (New Exception("Issues while stopping QTP: " & errMsg))
        Else
            QTP.App.Quit()
        End If
    End Sub


    Public Shared Function runCommand(ByVal sCommand, ByRef ret, ByRef errMsg) 'this is client that sends commands to listener

        Dim tcpclnt As TcpClient
        Dim stm As Stream
        Dim asen As ASCIIEncoding
        Dim ba As Byte()
        Dim bb(1024) As Byte
        Dim k, infoBackFrmListener

        tcpclnt = New TcpClient()
        tcpclnt.Connect("127.0.0.1", QTP._port)
        stm = tcpclnt.GetStream()
        asen = New ASCIIEncoding()
        ba = asen.GetBytes(sCommand)
        stm.Write(ba, 0, ba.Length)
        k = stm.Read(bb, 0, bb.Length)
        ret = ""
        errMsg = ""
        infoBackFrmListener = ""
        For i = 0 To k - 1 'we get dat in bytes. converting it to string for validation
            infoBackFrmListener = infoBackFrmListener + Convert.ToChar(bb(i))
        Next
        tcpclnt.Close()
        ' We get || seperated info back from listerner. first part holds return value, second holds error message if any.
        ret = Left(infoBackFrmListener, InStr(infoBackFrmListener, "||") - 1)
        errMsg = Mid(infoBackFrmListener, InStr(infoBackFrmListener, "||") + 2)
        If errMsg = "SUCCESS" Then
            runCommand = True
        Else
            runCommand = False
        End If
    End Function

End Class
