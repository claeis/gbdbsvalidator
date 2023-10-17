package ch.ehi.gbdbsvalidator.impl;

public class EgridGenerator {

	/*
	'  ***         PraefixText  =   Praefix (Mussparameter - 1. Teil als Ausgangsbasis fuer die E-GRID
	'  ***                          -> Das Praefix wird zentral beim Bund geholt (zufaellige Zuteilung)
	'  ***                             und als Parameter dem Algorithmus bereitgestellt.
	'  ***                             Format: genau 4 Stellen, nur Ziffern -> Zahl mit fuehrenden Nullen;
	'  ***                                     -> z.B. "1023", "0013"
	'  ***
	'  ***         Zaehler      =   Zaehler (Mussparameter - 2. Teil als Ausgangsbasis fuer die E-GRID)
	'  ***                          -> Innerhalb des Praefix-Bereiches wird fortlaufend nummeriert.
	'  ***                             Der Zaehler ist von jedem GB- oder AV-System intern zu verwalten.
	'  ***                             Fuer eine Vergabe einer jeweils einmaligen Zahl innerhalb eines
	'  ***                             Praefixes ist das GB- oder AV-System zustaendig (Kanton/SW-Hersteller).
	'  ***                             Der Zaehler wird Parameter dem Algorithmus bereitgestellt.
	'  ***                             Format: 1 bis 6 Stellen, numerisch, Wert zwischen "1" und "999'999"
	'  ***
	'  ***
	'  ***         RUeCKGABEWERT =   uebergibt True oder False!
	'  ***                          -> True:  Falls die E-GRID i.O. ist und genutzt werden kann;
	'  ***                          -> False: Falls die E-GRID falsch ist und NICHT verwendet werden darf!
	Public Function Algorithmus(ByRef EGRIDText As String, ByVal PraefixText As String, _
	  ByVal Zaehler As Long) As Boolean
	        
	        Dim EGRIDNum
	        Dim PZ As Integer
	        Dim EGRIDPZ As String * 2
	        Dim LZText As String * 1
	        Dim EGRIDTextNeu As String
	        Dim LZ, F As Byte
	        Dim OK As Boolean

	'  ***
	'  ***         Error werfen
	'  ***         Ueberpruefung auf Uebergabe korrekter Parameter
	'  ***

	        If Not (Len(PraefixText) = 4 And IsNumeric(PraefixText)) Then
	          Err.Raise 1001, "Algorithmus", "Format des Praefix ist ungueltig"
	        End If
	        
	        If Zaehler < 1 Or Zaehler > 999999 Then
	          Err.Raise 1002, "Algorithmus", "Zaehler ist ungueltig"
	        End If
	        
	        ' Praefix und Zaehler ergeben eine 10-stellige Ziffernfolge / String
	        EGRIDText = PraefixText + Right$("000000" + CStr(Zaehler), 6)
	        
	              
	'  ***
	'  ***         Anonymisierung 1: Ziffern veraendern
	'  ***         -> Jede Ziffer des 10-stelligen Strings um "7" erhoehen
	'  ***
	        
	        EGRIDTextNeu = ""
	        
	        For F = 1 To 10
	            LZText = CStr((CByte(Mid$(EGRIDText, F, 1)) + 7) Mod 10)
	            EGRIDTextNeu = EGRIDTextNeu & LZText
	        Next F
	        
	        EGRIDText = EGRIDTextNeu
	        
	        
	'  ***
	'  ***         Keine E-GRID die mit "0" anfaengt - keine fuehrende Null!
	'  ***         -> die 9. Spalte wird zur 1. Spalte!
	'  ***

	        ' kuenftige 1. Spalte bzw. 1. Ziffer
	        LZ = CByte(Mid$(EGRIDText, 9, 1))

	        ' wird nach dem Spaltentausch die 1. Ziffer keine "0" sein?
	        If LZ > 0 Then
	        
	'  ***
	'  ***         Anonymisierung 2:     Spalten tauschen
	'  ***
	'  ***         - Die letzten beiden Spalten (9,10) werden immer zu den Ersten (1,2)
	'  ***         - Abhaengig von der letzten Ziffer (10), werden die uebrigen Spalten
	'  ***           unterschiedlich vertauscht.
	              
	            ' letzte Spalte/Ziffer als Unterscheidungsmerkmal
	            LZ = CByte(Mid$(EGRIDText, 10, 1))
	    
	            ' Der Austauschmechanismus ist fest vorgeschrieben !!!
	            Select Case LZ
	                Case 1
	                    EGRIDNum = CDec(Mid$(EGRIDText, 9, 2) + Mid$(EGRIDText, 1, 2) + Mid$(EGRIDText, 7, 2) + Mid$(EGRIDText, 3, 2) + Mid$(EGRIDText, 5, 2))
	                Case 2
	                    EGRIDNum = CDec(Mid$(EGRIDText, 9, 2) + Mid$(EGRIDText, 5, 2) + Mid$(EGRIDText, 7, 2) + Mid$(EGRIDText, 3, 2) + Mid$(EGRIDText, 1, 2))
	                Case 3
	                    EGRIDNum = CDec(Mid$(EGRIDText, 9, 2) + Mid$(EGRIDText, 3, 2) + Mid$(EGRIDText, 5, 2) + Mid$(EGRIDText, 1, 2) + Mid$(EGRIDText, 7, 2))
	                Case 4
	                    EGRIDNum = CDec(Mid$(EGRIDText, 9, 2) + Mid$(EGRIDText, 1, 2) + Mid$(EGRIDText, 3, 2) + Mid$(EGRIDText, 5, 2) + Mid$(EGRIDText, 7, 2))
	                Case 5
	                    EGRIDNum = CDec(Mid$(EGRIDText, 9, 2) + Mid$(EGRIDText, 7, 2) + Mid$(EGRIDText, 3, 2) + Mid$(EGRIDText, 5, 2) + Mid$(EGRIDText, 1, 2))
	                Case 6
	                    EGRIDNum = CDec(Mid$(EGRIDText, 9, 2) + Mid$(EGRIDText, 5, 2) + Mid$(EGRIDText, 3, 2) + Mid$(EGRIDText, 1, 2) + Mid$(EGRIDText, 7, 2))
	                Case 7
	                    EGRIDNum = CDec(Mid$(EGRIDText, 9, 2) + Mid$(EGRIDText, 1, 2) + Mid$(EGRIDText, 5, 2) + Mid$(EGRIDText, 7, 2) + Mid$(EGRIDText, 3, 2))
	                Case 8
	                    EGRIDNum = CDec(Mid$(EGRIDText, 9, 2) + Mid$(EGRIDText, 7, 2) + Mid$(EGRIDText, 3, 2) + Mid$(EGRIDText, 1, 2) + Mid$(EGRIDText, 5, 2))
	                Case 9
	                    EGRIDNum = CDec(Mid$(EGRIDText, 9, 2) + Mid$(EGRIDText, 3, 2) + Mid$(EGRIDText, 5, 2) + Mid$(EGRIDText, 7, 2) + Mid$(EGRIDText, 1, 2))
	                Case 0
	                    EGRIDNum = CDec(Mid$(EGRIDText, 9, 2) + Mid$(EGRIDText, 5, 2) + Mid$(EGRIDText, 1, 2) + Mid$(EGRIDText, 7, 2) + Mid$(EGRIDText, 3, 2))
	            End Select
	            
	    
	'  ***
	'  ***         Pruefziffer Modulo 97
	'  ***
	'  ***         Im Prinzip waere dieser Befehl richtig, wird aber im VB zum Ueberlauf:
	'  ***         PZ = 97 - EGRIDNum Mod 97
	'  ***
	   
	            ' manuelle programmiertes Mod 97
	            PZ = 97 - Round(((EGRIDNum / 97) - Fix(EGRIDNum / 97)) * 97, 0)
	       
	            If PZ = 0 Then
	                EGRIDPZ = "00"
	            Else
	                EGRIDPZ = Right("0" + CStr(PZ), 2)
	            End If
	            
	            
	'  ***
	'  ***         Fertige gueltige E-GRID inkl. "CH" und Pruefziffer     :-)
	'  ***

	            EGRIDText = "CH" + Right$("000000000" + CStr(EGRIDNum), 10) + EGRIDPZ
	            OK = True
	            
	        Else
	        
	'  ***
	'  ***         Unbrauchbare E-GRID mit einer fuehrenden "0"
	'  ***

	            EGRIDText = ""
	            OK = False
	        
	        End If
	        
	        ' Rueckgabe
	        Algorithmus = OK

	End Function
	*/
	public static long Algorithmus(StringBuffer egrid,String PraefixText,long Zaehler)
	{
			        
		int LZ;
		int PZ;
		String EGRIDPZ;
		String LZText;
		String EGRIDTextNeu;
		String EGRIDText;
		Zaehler++;
		
		while(true){
			//         Error werfen
			//         Ueberpruefung auf Uebergabe korrekter Parameter

			if( !(PraefixText.length()==4 && Long.parseLong(PraefixText)>0)){
				throw new IllegalArgumentException("Format des Praefix ist ungueltig <"+PraefixText+">");
			}

			if(Zaehler < 1 || Zaehler > 999999){
				throw new IllegalArgumentException("Zaehler ist ungueltig");
			}

			// Praefix und Zaehler ergeben eine 10-stellige Ziffernfolge / String
			EGRIDText = PraefixText + right("000000" + Long.toString(Zaehler), 6);


			//         Anonymisierung 1: Ziffern veraendern
			//         -> Jede Ziffer des 10-stelligen Strings um "7" erhoehen

			EGRIDTextNeu = "";

			for(int f = 0;f<10;f++){
				LZText = Integer.toString((Integer.parseInt(EGRIDText.substring(f, f+1)) + 7) % 10);
				EGRIDTextNeu = EGRIDTextNeu + LZText;
			}

			EGRIDText = EGRIDTextNeu;


			//         Keine E-GRID die mit "0" anfaengt - keine fuehrende Null!
			//         -> die 9. Spalte wird zur 1. Spalte!


			// kuenftige 1. Spalte bzw. 1. Ziffer
			LZ = Integer.parseInt(EGRIDText.substring(8, 9));

			// wird nach dem Spaltentausch die 1. Ziffer keine "0" sein?
			if(LZ > 0){
				break;
			}
			Zaehler++;
		}

		//         Anonymisierung 2:     Spalten tauschen
		//
		//         - Die letzten beiden Spalten (9,10) werden immer zu den Ersten (1,2)
		//         - Abhaengig von der letzten Ziffer (10), werden die uebrigen Spalten
		//           unterschiedlich vertauscht.

		// letzte Spalte/Ziffer als Unterscheidungsmerkmal
		LZ = Integer.parseInt(EGRIDText.substring(9, 10));

		String EGRIDNum="";

		// Der Austauschmechanismus ist fest vorgeschrieben !!!
		switch(LZ){
		case 1:
			EGRIDNum = EGRIDText.substring(8, 10) + EGRIDText.substring(0, 2) + EGRIDText.substring(6, 8) + EGRIDText.substring(2, 4) + EGRIDText.substring(4, 6);
			break;
		case 2:
			EGRIDNum = EGRIDText.substring(8, 10) + EGRIDText.substring(4, 6) + EGRIDText.substring(6, 8) + EGRIDText.substring(2, 4) + EGRIDText.substring(0, 2);
			break;
		case 3:
			EGRIDNum = EGRIDText.substring(8, 10) + EGRIDText.substring(2, 4) + EGRIDText.substring(4, 6) + EGRIDText.substring(0, 2) + EGRIDText.substring(6, 8);
			break;
		case 4:
			EGRIDNum = EGRIDText.substring(8, 10) + EGRIDText.substring(0, 2) + EGRIDText.substring(2, 4) + EGRIDText.substring(4, 6) + EGRIDText.substring(6, 8);
			break;
		case 5:
			EGRIDNum = EGRIDText.substring(8, 10) + EGRIDText.substring(6, 8) + EGRIDText.substring(2, 4) + EGRIDText.substring(4, 6) + EGRIDText.substring(0, 2);
			break;
		case 6:
			EGRIDNum = EGRIDText.substring(8, 10) + EGRIDText.substring(4, 6) + EGRIDText.substring(2, 4) + EGRIDText.substring(0, 2) + EGRIDText.substring(6, 8);
			break;
		case 7:
			EGRIDNum = EGRIDText.substring(8, 10) + EGRIDText.substring(0, 2) + EGRIDText.substring(4, 6) + EGRIDText.substring(6, 8) + EGRIDText.substring(2, 4);
			break;
		case 8:
			EGRIDNum = EGRIDText.substring(8, 10) + EGRIDText.substring(6, 8) + EGRIDText.substring(2, 4) + EGRIDText.substring(0, 2) + EGRIDText.substring(4, 6);
			break;
		case 9:
			EGRIDNum = EGRIDText.substring(8, 10) + EGRIDText.substring(2, 4) + EGRIDText.substring(4, 6) + EGRIDText.substring(6, 8) + EGRIDText.substring(0, 2);
			break;
		case 0:
			EGRIDNum = EGRIDText.substring(8, 10) + EGRIDText.substring(4, 6) + EGRIDText.substring(0, 2) + EGRIDText.substring(6, 8) + EGRIDText.substring(2, 4);
			break;
		}

		//         Pruefziffer Modulo 97
		//
		//         Im Prinzip waere dieser Befehl richtig, wird aber im VB zum Ueberlauf:
		//         PZ = 97 - EGRIDNum Mod 97
		PZ = (int)(97L - Long.parseLong(EGRIDNum) % 97L);

		if( PZ == 0){
			EGRIDPZ = "00";
		}else{
			EGRIDPZ = right("0" + Integer.toString(PZ), 2);
		}


		//         Fertige gueltige E-GRID inkl. "CH" und Pruefziffer     :-)
		egrid.setLength(0);
		egrid.ensureCapacity(14);
		egrid.append("CH");
		egrid.append(EGRIDNum);
		egrid.append(EGRIDPZ);

		return Zaehler;
	}
	public static String right(String value,int length)
	{
		int len=value.length();
		if(len<length){
			return value;
		}
		return value.substring(len-length);
	}
	public static boolean validateEgrid(String egrid){
		if(egrid.length()!=14){
			return false;
		}
		String value=egrid.substring(2,12);
		String pz=egrid.substring(12);
		//System.out.println("value <"+value+">");
		//System.out.println("pz <"+pz+">");
		int calcPz = (int)(97L - Long.parseLong(value) % 97L);
		//System.out.println("calcpz <"+calcPz+">");
		if(Integer.parseInt(pz)!=calcPz){
			return false;
		}

		return true;
	}
	static public void main(String[] args)
	{
		String prefix="4955"; // "1234";
		long zaehler=107   ;// 1234 Zaehler 001002
		StringBuffer egrid=new StringBuffer();
		for(int i=0;i<20;i++){
			zaehler=Algorithmus(egrid, prefix, zaehler);
			System.out.println(zaehler+" "+egrid.toString());
			//if(egrid.toString().equals("CH907496138209")){
			//	break;
			//}
		}
	}
}
