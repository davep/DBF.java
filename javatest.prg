Function Main( cRecs )
Local nRecs := 20
Local n

   If cRecs != NIL
      nRecs := max( val( cRecs ), 1 )
   EndIf

   Use JavaTest New Exclusive
   Zap

   For n := 1 To nRecs
      dbappend()
      JavaTest->Test_Char := "Test String " + alltrim( str( n ) )
      JavaTest->Test_Num  := n
      JavaTest->Test_Date := date() + n
      JavaTest->Test_Log  := ( ( n % 2 ) == 0 )
   Next

   dbclosearea()

Return( NIL )
