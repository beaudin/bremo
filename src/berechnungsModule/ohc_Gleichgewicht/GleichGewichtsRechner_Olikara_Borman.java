package berechnungsModule.ohc_Gleichgewicht;




import java.util.Hashtable;

import bremo.parameter.CasePara;

import misc.PhysKonst;
import kalorik.spezies.Spezies;

	public class GleichGewichtsRechner_Olikara_Borman extends GleichGewichtsRechner{
		
		protected GleichGewichtsRechner_Olikara_Borman(CasePara cp,
				String gleichGewichtsKonstantenVorgabe,double T_freeze){
			super(cp,gleichGewichtsKonstantenVorgabe,T_freeze);
		}	
		
		protected  double [] calc_gg(	double p, 
										double T,
										double lambda, 
										double dAnzC,
										double dAnzH,
										double dAnzO,
										double dAnzN,
										double dAnzAr){ 
			
			double fMoleH2OLad=0;
			double fAbgasLad=0;		
			double fAtomeS=0;
			
			double [] adX=new double [14];	
			
//			double T_freeze=Bremo.get_casePara().SYS.T_FREEZE;
			
			if(T<T_FREEZE)T=T_FREEZE; //aus der Elternklasse geerbt
			
			double p_bar= p*0.00001; //Umrechnung von Pascal in bar
			

//			double fO2min =cKrst+hKrst/4- oKrst/2;
//			double fAtomeC = cKrst;
//			double fAtomeH = hKrst;
//			double fAtomeO = oKrst;
//			double fAtomeN = nKrst;
//			double fAtomeS = 0;
//			
//			double fVol_CO2Luft=PhysKonst.get_vol_CO2_Luft();
//			double fVol_O2Luft=PhysKonst.get_vol_O2_Luft();
//			double fVol_N2Luft=PhysKonst.get_vol_N2_Luft();
//			double fVol_ArLuft = PhysKonst.get_vol_Ar_Luft();
				
			// Hilfsgroessen:
			   int iZ1=12345,iZ2=12345,iGenau=12345;
			   
			   
			   double dAnzHzC=12345,dAnzOzC=12345,dAnzNzC=12345,dAnzArzC=12345;
			   double dSQ_pAtm=12345;
			   double dSQ_X4=12345,dSQ_X6=12345,dSQ_X8=12345,dSQ_X11=12345;
			   double dHilf1=12345,dHilf2=12345,dHilf3=12345,dX8alt=12345;
			   double dSummeAbgas=12345; //,dSummeAbgasNorm;


			// Zwischenwerte:
			   double dC1=12345,dC2=12345,dC3=12345,dC5=12345,dC7=12345,dC9=12345,dC10=12345;
			   double dDeltaX8=12345;
			   double dT14=12345,dT28=12345,dT311=12345,dT54=12345,dT58=12345,dT78=12345;
			   double dT711=12345,dT94=12345,dT98=12345,dT106=12345,dT108=12345;

			// Elemente der Matrix:
			   double dA11=12345,dA12=12345,dA13=12345,dA14=12345,dA21=12345,dA22=12345;
			   double dA23=12345,dA24=12345,dA31=12345,dA32=12345,dA33=12345,dA34=12345;
			   double dA41=12345,dA42=12345,dA43=12345,dA44=12345;
			   
			   double dA1=12345,dA2=12345,dA3=12345,dA4=12345;

			// Hilfsgroessen GAUSS:
			   double dB1=12345,dB2=12345,dB3=12345,dB4=12345;
			   double dB11=12345,dB12=12345,dB13=12345,dB14=12345,dB22=12345,dB23=12345;
			   double dB24=12345,dB33=12345,dB34=12345,dB44=12345;
			   double dC21=12345,dC31=12345,dC32=12345,dC41=12345,dC42=12345,dC43=12345;
			   double dX1=12345,dX2=12345,dX3=12345,dX4=12345;


			   
			

			// ÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄ
//			                              Anweisungen
			// ÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄ



			// Hilfsvariablen:
			// ================

			   iZ2=0;       //Schleifenzaehler

//			// Zusammensetzung der Reaktionspartner (Krst, Luft, zusaetzl. H2O)
//			// Restgas wird nicht betrachtet:
//			// ----------------------------------------------------------------
//			// fO2min: stoechiometrischer Sauerstoffbedarf [molO2/molKrst]
//			// dLambdaO2min: zugefuehrter Sauerstoff [molO2/molKrst]
//			// Zusammensetzung der Luft:
//			// ca. 21% Sauerstoff, ca. 78% Stickstoff, ca. 0.03% Kohlenioxid
//			// Zusammensetzung des Kraftstoffs:
//			// in den meisten Faellen Komposit aus C-H-O-(N)
//
//			   
//			   
//			// dLambdaO2min=LambdaV*(AnzC-Atome + AnzH-Atome/4 + AnzO-Atome/2)des Krst (KOEF)
//				dLambdaO2min=lambda*fO2min;
//			// C-Atome der Reaktanten (Kohlenstoffbilanz):
////					Kohlenstoffatome aus Krst + Kohlenstoff aus Luft
////			    AnzC-Atome aus Krst + Molenbruch CO2/O2*KOEF    
////			                                    (0.0014*KOEF)
//			   dAnzC=fAtomeC+dLambdaO2min*fVol_CO2Luft/fVol_O2Luft;
//			// H-Atome der Reaktanten (Wasserstoffbilanz): 
////					Wasserstoff aus Krst + Wasserstoff aus feuchter Luft + Wasserstoff aus zusaetz. Wasser
////			    AnzH-Atome aus Krst + 2*Molenbruch H2O/Krst
//				dAnzH=fAtomeH+2*fMoleH2OLad;
//			// O-Atome der Reaktanten (Sauerstoffbilanz):
////					Sauerstoff aus Krst + Sauerstoff aus O2-Gehalt der Luft + Sauerstoff aus CO2-Gehalt der Luft + Sauerstoff aus zusaetzl Wasser
////			    AnzO-Atome aus Krst + 2*KOEF*Molenbruch O2/O2 + 2*KOEF*Molenbruch CO2/O2 + Molenbruch H2O/Krst
////			                         (2*KOEF*1)                (2*KOEF*0.0014)
//			   dAnzO=fAtomeO+2*dLambdaO2min*(1+fVol_CO2Luft/fVol_O2Luft)+fMoleH2OLad;
//			// N-Atome der Reaktanten (Stickstoffbilanz):
////					(Stickstoff aus Krst) + Stickstoff aus Luft
////					(AnzN-Atome aus Krst) + 2*KOEF*Molenbruch N2/O2
////			                           (2*KOEF*3.7274)		   
//		
//			   dAnzN=fAtomeN+2*dLambdaO2min*fVol_N2Luft/fVol_O2Luft;
			  
			// Ar-Atome de Reaktanten (Argonbilanz):
//					Argon der Luft
//					KOEF*Molenbruch Ar/O2
//			   dAnzAr=dLambdaO2min*fVol_ArLuft/fVol_O2Luft;

			   dAnzHzC=dAnzH/dAnzC;
			   dAnzOzC=(2*fAtomeS-dAnzO)/dAnzC;
			   dAnzNzC=dAnzN/dAnzC;
			   dAnzArzC=(fAtomeS+dAnzAr)/dAnzC;			

				// Berechnung nach Olikara und Bormann erfolgt fuer Temperatur mit Rankine
				// (1R=1.8K) bzw. fuer Druck in PSI (1bar=14.50368PSI) (1atm=14.696PSI):
				// weitere Umrechnungen:  1atm=1.01325bar
				// --------------------------------------------------------------------------
				   // bisher falsch: dSQ_pAtm=sqrt(p*1.01325);    korrigiert Juni 2004        
				   dSQ_pAtm=Math.sqrt(p_bar/1.01325);					//[bar] in [atm]
				   

				// Berechnung der Konstanten C(i)=f(T,p) mit den Gleichgewichtskonstanten Kp
				// Gln. Kp(T) stammen von Olikara/Bormann nach Werten aus JANAF-Tabelle
				// Gueltigkeitsbereich: 600øK <= T <= 4000øK
				// =========================================================================
			   
			   
			   	//Berechnung der Gleichgewichtskonstanten	
				double Kp []=new double [8];
				Kp=get_Kp(T);
				
				//Umrechnung von Gleichgewichtskonstanten für Partialdrücke hier in Atmosphären 
				//in Gleichgewichtskonstanten für Konzentrationen
				
							
				//1/2 H2 --> H
				dC1=Kp[4]/dSQ_pAtm;	
				
				//1/2 O2 --> O
				dC2=Kp[5]/dSQ_pAtm;			
				
				//1/2 N2 --> N
				dC3=Kp[6]/dSQ_pAtm;
				
				//1/2 O2 +1/2 H2 --> OH
				dC5=Kp[3];
				
				//1/2 N2 + 1/2 O2 --> NO
				dC7=Kp[7];
				
				//H2+ 1/2 O2 --> H2O
				dC9=Kp[2]*dSQ_pAtm;	
			   
				//CO + 1/2O2-->CO2
				dC10=(1/Kp[1])*dSQ_pAtm;	
			   



			// Berechnung von Naeherungsloesungen fuer X[4],X[6],X[8],X[11]
			// mit vereinfachter Abgaszusammensetzung:
			// ======================================================================================

			// Fuer die erste Naeherung wird zunaechst mit undissoziertem Verbrennungsgas gerechnet.
			// D.h. Reduzierung der Gleichgewichtsbetrachtung auf das Wassergasgleichgewicht.
//				Erst dann kann man die genaue Abgaszusammensetzung mit Dissoziation berechnen.
//				vereinfachte Reaktionshauptgleichung:
			// CAX HY OAZ + Lambda*(AX+Y/4+AZ/2)(O2+Molenbruch N2/O2*N2) --> CO2+CO+H2O+H2+O2+N2

			// Abschaetzung von X[13]:
			// adX[13] ist Anzahl der Mole Kraftstoff, die 1 Mol Abgas ergeben (Molenbruch Abgas/Krst)
			// Geht man davon aus, dass die der Ansaugluft zugemischte Abgasmenge den Zylinder in 
//				gleicher Form wieder verlaesst, so ergeben nicht X_13 Mole Kraftstoff genau 1 Mol Abgas, 
//				sondern es gilt: 
//				X_13 Mole Krst i.d.Ladung + X_AbgasLad Mole Restgas i.d.Ladung --> 1 Mol Abgas (i.d.Ladung)
			// Fuer die Mole Abgas, die aus der Verbrennung des Kraftstoffes resultieren, gilt somit:
			// X_13 Mole Krst --> 1 Mol Abgas - X_AbgasLad Mole Restgas :
			// --------------------------------------------------------------------------------------
			   
				if (lambda < 1) 
				// entspricht fLambdaTr < 1
				// Da stoechiometrisch gesehen Sauerstoffmolekuele fehlen, kann man davon ausgehen, dass
				// saemtlicher Sauerstoff reduziert wird.
			   {
					adX[13]=(1-fAbgasLad)/(0.5*(dAnzH+dAnzN)+dAnzC+dAnzAr+fAtomeS);
			   }
				else
				// entspricht fLambdaTr >= 1
				// Da genug Sauerstoff zum Verbrennen vorhanden ist, kann man davon ausgehen, dass 
				// saemtliche H2 verbrannt sind. Ferner kann davon ausgegangen werden, dass alles 
				// CO in CO2 oxidiert ist
			   {
					adX[13]=(1-fAbgasLad)/(0.5*(0.5*dAnzH+dAnzO+dAnzN)+dAnzAr+fAtomeS);
				}
				


			// Berechnung Naeherung von X[8] durch Iteration:
			// ---------------------------------------------
				adX[8]=0.1;                                     //Startwerte der Iteration
				dDeltaX8=0;
				dX8alt=0;
				iZ1=0;

			// Ueberpruefung von erstem Naeherungswert fuer X[8],
			// verhindert negativen Wert fuer X[8]!:
				iZ1=0;
				do
				{
					iZ1++;                                                  //Schleifenzaehler
					
					adX[8]=adX[8]*0.1;
					
				// Abbruch der Schleife, wenn zuviele Schritte:
					if (iZ1 > 100000)
					{
						System.out.println("Olokara Borman - iteration A adX[8] > 100000 ln403");
					}
					

				// Fehlerueberpruefung (neg. Radikant der Wurzel):
					if (adX[8]<0)
					{
						System.out.println("Olokara Borman - iteration A negatives dX[8] ln411");
					}

					dSQ_X8= Math.sqrt(adX[8]);                               //Hilfsgroessen
					dHilf1=(dC10*dSQ_X8+1);
					dHilf2=(dC9*dSQ_X8+1);
					dHilf3=(2*dC10*dSQ_X8+1)*dAnzC/dHilf1+dC9*dSQ_X8*dAnzH*0.5/dHilf2+2*
																	adX[8]/adX[13]-dAnzO;				
					

				} while (dHilf3 > 0);                                  //Ende derSchleife



			// Iterationsschleife 1 fuer Naeherung X[8]:
			// ---------------------------------------
				iZ1=0;

				do       //Genauigkeitskriterium    do
				{
					iZ1++;                                                 //Schleifenzaehler

				// Fehlerueberpruefung (neg. Radikant der Wurzel):
					if (adX[8]<0)
					{
						System.out.println("Olokara Borman - iteration B negatives ddX[8]");
					}

					dSQ_X8=Math.sqrt(adX[8]);                               //Hilfsgroessen
					dHilf1=(dC10*dSQ_X8+1);
					dHilf2=(dC9*dSQ_X8+1);

					dX8alt=adX[8];

					dDeltaX8=-((2*dC10*dSQ_X8+1)*dAnzC/dHilf1+dC9*dSQ_X8*dAnzH*0.5/dHilf2+
								2*adX[8]/adX[13]-dAnzO)/(dC10*dAnzC*0.5/(dSQ_X8*dHilf1*dHilf1)
												 +dC9*dAnzH*0.25/(dSQ_X8*dHilf2*dHilf2)+2/adX[13]);


					adX[8]=dX8alt+dDeltaX8;

				// Abbruch der Iteration, wenn zuviele Schritte:
					if (iZ1 > 100000)
					{
						System.out.println("Olokara Borman - iteration B adX[8] > 100000 ln462");
					}

				} while (Math.abs(dDeltaX8) > .00001 && iZ1<1001);                                               //Ende der Iteration


				
			// Berechnung Naeherung von X[4],X[6],X[11]:
			// ----------------------------------------
				adX[4]=0.5*adX[13]*dAnzH/(1+dC9*(Math.sqrt(adX[8])));
				adX[6]=adX[13]*dAnzC/(1+dC10*(Math.sqrt(adX[8])));
				adX[11]=adX[13]*0.5*dAnzN;


			// Berechnung der genauen Molenbrueche
			// Zusammensetzung des dissoziierten Verbrennungsgases:
			// ====================================================

			// Beginn der 2.ten Iterationsschleife:
			// ------------------------------------

			// Sprungmarke, wenn Summe der Mole Abgas falsch:
			double deltaSumAbgas;
			do{

				iGenau=1;                          //Hilfsgroesse zur Genauigkeitsueberpruefung
				iZ1=0;

					do
					{
		
					// Fehlerueberpruefung (neg. Radikant der Wurzel):
					// ---------------------------------------------
						if (adX[4]<=0 || adX[6]<0 || adX[8]<=0 || adX[11]<=0)
						{
							System.out.println("Olokara Borman - Molenbrueche negativ oder 0,-> neg. Radikant, Div. 0 (It. 2)! ln503");
		
						}
		
					// Hilfsvariablen:
					// ---------------
						dSQ_X4=(float) Math.sqrt(adX[4]);
						dSQ_X6=(float) Math.sqrt(adX[6]);
						dSQ_X8=(float) Math.sqrt(adX[8]);
						dSQ_X11=(float) Math.sqrt(adX[11]);
		
					// Berechnung der Molenbrueche X[1],X[2],X[3],X[5],X[7],X[9],X[10] nach
					// den Gleichgewichtsreaktionen:
					// -------------------------------------------------------------------
						adX[1]=dC1*dSQ_X4;
						adX[2]=dC2*dSQ_X8;
						adX[3]=dC3*dSQ_X11;
						adX[5]=dC5*dSQ_X4*dSQ_X8;
						adX[7]=dC7*dSQ_X8*dSQ_X11;
						adX[9]=dC9*adX[4]*dSQ_X8;
						adX[10]=dC10*adX[6]*dSQ_X8;
		
					// partielle Ableitungen:
					// ----------------------
						dT14=.5F*dC1/dSQ_X4;
						dT28=.5F*dC2/dSQ_X8;
						dT311=.5F*dC3/dSQ_X11;
						dT54=.5F*dC5/dSQ_X4*dSQ_X8;
						dT58=.5F*dC5/dSQ_X8*dSQ_X4;
						dT78=.5F*dC7/dSQ_X8*dSQ_X11;
						dT711=.5F*dC7/dSQ_X11*dSQ_X8;
						dT94=dC9*dSQ_X8;
						dT98=.5F*dC9/dSQ_X8*adX[4];
						dT106=dC10*dSQ_X8;
						dT108=.5F*dC10/dSQ_X8*adX[6];
		
					// Elemente der Matrix fuer GAUSS-Elimination:
					// ------------------------------------------
						dA11=dT14+2+dT54+2*dT94;
						dA12=-dAnzHzC*(1+dT106);
						dA13=dT58+2*dT98-dAnzHzC*dT108;
						dA14=0;
						dA21=dT54+dT94;
						dA22=1+2*dT106+dAnzOzC*(1+dT106);
						dA23=dT28+dT58+dT78+2*(1+dT108)+dT98+dT108*dAnzOzC;
						dA24=dT711;
						dA31=0;
						dA32=-dAnzNzC*(1+dT106);
						dA33=dT78-dAnzNzC*dT108;
						dA34=dT311+dT711+2;
						dA41=dT14+dT54+1+dT94;
						dA42=1+dT106+dAnzArzC*(1+dT106);
						dA43=dT28+dT58+dT78+1+dT98+dT108+dAnzArzC*dT108;
						dA44=dT311+dT711+1;
		
						dA1=-(adX[1]+2*(adX[4]+adX[9])+adX[5])+dAnzHzC*(adX[6]+adX[10]);
						dA2=-(adX[2]+adX[5]+adX[6]+adX[7]+adX[9]+2*(adX[8]+adX[10])+dAnzOzC*(adX[6]+adX[10]));
						dA3=-(adX[3]+adX[7]+2*adX[11])+dAnzNzC*(adX[6]+adX[10]);
						dA4=-(adX[1]+adX[2]+adX[3]+adX[4]+adX[5]+adX[6]+adX[7]+adX[8]+adX[9]+
						//            adX[10]+adX[11])-dAnzArzC*+(adX[6]+adX[10])+1;
										  adX[10]+adX[11])-dAnzArzC*(adX[6]+adX[10])+1-fAbgasLad;
		
					
					// Loesung des linearen Gleichungssystems mit Hilfe des
					// GAUSS-Eliminationsverfahren
					// ---------------------------------------------------
		
					// Es gilt: dB(i,1)=dA(i,1):
					// -------------------------
						dB11=dA11;
						dB12=dA12;
						dB13=dA13;
						dB14=dA14;
		
					// Berechnung dC(i,1):
					// -------------------
						dC21=-dA21/dA11;
						dC31=-dA31/dA11;
						dC41=-dA41/dA11;
		
					// Berechnung dC(i,k) und dB(i,k):
					// -------------------------------
						dB22=dA22+dC21*dB12;
						dB23=dA23+dC21*dB13;
						dB24=dA24+dC21*dB14;
		
						dC32=(dA32+dC31*dB12)/-dB22;
		
						dB33=dA33+dC31*dB13+dC32*dB23;
						dB34=dA34+dC31*dB14+dC32*dB24;
		
						dC42=(dA42+dC41*dB12)/-dB22;
						dC43=(dA43+dC41*dB13+dC42*dB23)/-dB33;
		
						dB44=dA44+dC41*dB14+dC42*dB24+dC43*dB34;
		
		
					// Berechnung B(i):
					// ----------------
						dB1=dA1;
						dB2=dA2+dC21*dB1;
						dB3=dA3+dC31*dB1+dC32*dB2;
						dB4=dA4+dC41*dB1+dC42*dB2+dC43*dB3;
		
					// Berechnung der 4 Loesungen X(i):
					// X1=dX[4],X2=dX[6],X1=dX[8],X1=dX[11]
					// ------------------------------------
						dX4=dB4/dB44;
						dX3=(-dB3+dB34*dX4)/-dB33;
						dX2=(-dB2+dB24*dX4+dB23*dX3)/-dB22;
						dX1=(-dB1+dB14*dX4+dB13*dX3+dB12*dX2)/-dB11;
						
//						System.out.println("Gauss-Iteration: "+ iZ1);
//						System.out.println(dX1 + " "+ dX2 + " " + dX3+ " "+ dX4 );		
		
					// neue X[4],X[6],X[8],X[11]:
					// --------------------------
						adX[4]=adX[4]+dX1;
						adX[6]=adX[6]+dX2;
						adX[8]=adX[8]+dX3;
						adX[11]=adX[11]+dX4;
		
		
					// Ueberpruefung, ob Genauigkeit erreicht ist:
					// -----------------------------------------
//						if (Math.abs(dX1) < 0.0000001 && Math.abs(dX2) < 0.0000001 && Math.abs(dX3) < 0.0000001 && Math.abs(dX4) < 0.0000001 && iZ1<1001)
//							iGenau=0;                       //Bedingung fuer Abbruch der Iteration
	//	
		
					// Nullsetzung von X[6] wenn negativ:
					// ----------------------------------
						if (adX[6] < 0)
							adX[6]=0;
		
						
					// Abbruch der Iteration, wenn zuviele Schritte:
					// ---------------------------------------------
						iZ1++;                                                  //Schleifenzaehler
						if (iZ1 > 10000)
						{
							System.out.println("Olokara Borman - Zuviele Iterationsschritte (It. 2)! ln647");
		
						}
						//Ende der Iterationsschleife	
					} while (Math.abs(dX1) < 0.0000001 && Math.abs(dX2) < 0.0000001 && 
							Math.abs(dX3) < 0.0000001 && Math.abs(dX4) < 0.0000001 && iZ1<1001);
					
					//} while (iGenau != 0);   //alte Methode                      

				
			// Berechnung der restlichen Molenbrueche:
			// ======================================

			// Berechnung der exakten Molenbrueche X[1],X[2],X[3],X[5],X[7],X[9],X[10]
			// nach den Gleichgewichtsreaktionen:
			// ----------------------------------------------------------------------
				adX[1]=dC1*dSQ_X4;                             //Molenbruch H/Abgas
				adX[2]=dC2*dSQ_X8;                             //Molenbruch O/Abgas
				adX[3]=dC3*dSQ_X11;                            //Molenbruch N/Abgas
				adX[5]=dC5*dSQ_X4*dSQ_X8;                      //Molenbruch OH/Abgas
				adX[7]=dC7*dSQ_X8*dSQ_X11;                     //Molenbruch NO/Abgas
				adX[9]=dC9*adX[4]*dSQ_X8;                      //Molenbruch H2O/Abgas
				adX[10]=dC10*adX[6]*dSQ_X8;                    //Molenbruch CO2/Abgas
			
			// Berechnung X[0], X[12] und X[13]:
			// --------------------------------------------------------------------------------------
//				adX[13]=(adX[6]+adX[10])/dAnzC;                  //Molenbruch Krst/Ladung
//				adX[12]=adX[13]*dAnzAr;                          //Molenbruch Ar/Abgas
//				adX[0]=adX[13]*fAtomeS;                          //Molenbruch S/Abgas

//				adX[13]=adX[13]*(1-fAbgasLad);

				// adX[13] ist Anzahl der Mole Kraftstoff, die 1 Mol Abgas ergeben (Molenbruch Krst/Ladung)
				// Geht man davon aus, dass die der Ansaugluft zugemischte Abgasmenge den Zylinder in 
				//	gleicher Form wieder verlaesst, so ergeben nicht X_13 Mole Kraftstoff genau 1 Mol Abgas, 
				//	sondern es gilt:
				//	X_13 Mole Krst i.d.Ladung + X_AbgasLad Mole Abgas i.d.Ladung --> 1 Mol Abgas (i.d.Ladung)
				// Fuer die Mole Abgas, die aus der Verbrennung des Kraftstoffes resultieren, gilt somit:
				// X_13 Mole Krst --> 1 Mol Abgas - X_AbgasLad Mole Abgas in der Ladung.
				// Kohlenstoffatome der Berechnung kommen aus Kraftstoff, allerdings reduziert um
				// den Restgasanteil:
				adX[13]=(adX[6]+adX[10])/dAnzC*(1-fAbgasLad);  //Molenbruch Ladung,tr/Abgas
				// Argon und Schwefel benoetigen wiederrum den Absolutwert:
				adX[12]=adX[13]*dAnzAr/(1-fAbgasLad);          //Molenbruch Ar/Abgas
				adX[0]=adX[13]*fAtomeS/(1-fAbgasLad);          //Molenbruch S/Abgas


			// Ueberpruefung, ob Summe der Molenbrueche d. Abgaskomponenten 
//				= 1-Restgasgehalt(intern + extern)ist:
//				(wenn Startwerte X[8],... zu ungenau, dann Newtonverfahren ungenau!)
			// ==============================================================================
				dSummeAbgas= adX[0]+adX[1]+adX[2]+adX[3]+adX[4]+adX[5]+adX[6]
								+adX[7]+adX[8]+adX[9]+adX[10]+adX[11]+adX[12];

				
					                                             //Schleifenzaehler
				

				// Iteration wird mit den letzten Werten fuer X[4],X[6],X[8],X[11] nochmal
				// durchgefuehrt:
				// -----------------------------------------------------------------------

					iZ2++;   
					
					if (iZ2 == 1000)
					{
						System.out.println("Olokara Borman - Zuviele Iterationsschritte (It. 3)! ln713");	
					}
					 deltaSumAbgas=Math.abs((dSummeAbgas-1+fAbgasLad));
					
					//System.out.println("Bei äusserer Iteration Nr.: " + iZ2 + " beträgt die Abweichung: " + deltaSumAbgas);
					
				}while(deltaSumAbgas>0.00001);//while (test >.0001);

			//System.out.println("Gesamtzhal der Iterationen: "+iZ2);



			   adX[0]=adX[0]/dSummeAbgas;
			   adX[1]=adX[1]/dSummeAbgas;
			   adX[2]=adX[2]/dSummeAbgas;
			   adX[3]=adX[3]/dSummeAbgas;
			   adX[4]=adX[4]/dSummeAbgas;
			   adX[5]=adX[5]/dSummeAbgas;
			   adX[6]=adX[6]/dSummeAbgas;
			   adX[7]=adX[7]/dSummeAbgas;
			   adX[8]=adX[8]/dSummeAbgas;
			   adX[9]=adX[9]/dSummeAbgas;
			   adX[10]=adX[10]/dSummeAbgas;
			   adX[11]=adX[11]/dSummeAbgas;
			   adX[12]=adX[12]/dSummeAbgas;
			   adX[13]=adX[13]/dSummeAbgas;				
			   
			   //Damit die Summe der molenbrueche eins ergibt wird N2 angepasst!		   
			   
			   adX[11]=1-(adX[1]+adX[2]+adX[3]+adX[4]+adX[5]+adX[6]
			                             +adX[7]+adX[8]+adX[9]+adX[10]);		
			

			return adX;	
			
		}
		
//		public Hashtable<String, Double> get_GG_molenBrueche(double p, double T,
//				double lambda, Kraftstoff krst){
//			Hashtable<String, Double> molenBruecheHash=new Hashtable<String, Double>(15);
//			double [] adX;
//			
//			adX=calc_gg(p,T,lambda,krst);
//			
////			  ³ adX[0]  = Molenbruch SO2 / Abgas                                       ³
////			  ³ adX[1]  = Molenbruch H / Abgas                                         ³
////			  ³ adX[2]  = Molenbruch O / Abgas                                         ³
////			  ³ adX[3]  = Molenbruch N / Abgas                                         ³
////			  ³ adX[4]  = Molenbruch H2 / Abgas                                        ³
////			  ³ adX[5]  = Molenbruch OH / Abgas                                        ³
////			  ³ adX[6]  = Molenbruch CO / Abgas                                        ³
////			  ³ adX[7]  = Molenbruch NO / Abgas                                        ³
////			  ³ adX[8]  = Molenbruch O2 / Abgas                                        ³
////			  ³ adX[9]  = Molenbruch H2O / Abgas                                       ³
////			  ³ adX[10] = Molenbruch CO2 / Abgas                                       ³
////			  ³ adX[11] = Molenbruch N2 / Abgas                                        ³
////			  ³ adX[12] = Molenbruch Ar / Abgas                                        ³
////			  ³ adX[13] = Molenbruch Kraftstoff / Ladung     
//			
//			
//			molenBruecheHash.put("H",adX[1]);
//			molenBruecheHash.put("O",adX[2]);
//			molenBruecheHash.put("N",adX[3]);
//			molenBruecheHash.put("H2",adX[4]);
//			molenBruecheHash.put("OH",adX[5]);
//			molenBruecheHash.put("CO",adX[6]);
//			molenBruecheHash.put("NO",adX[7]);
//			molenBruecheHash.put("O2",adX[8]);
//			molenBruecheHash.put("H2O",adX[9]);
//			molenBruecheHash.put("CO2",adX[10]);
//			molenBruecheHash.put("N2",adX[11]);
//			//molenBruecheHash.put("Ar",adX[12]);
//			molenBruecheHash.put("p_err",(MatLib.vecsum(adX,1,11)-p)*1e-5);
//			
//			return molenBruecheHash;
//			
//		}


		@Override
		public Hashtable<Spezies, Double> get_GG_molenBrueche(double p,
				double T, double lambda, double cKrst, double hKrst,
				double oKrst, double nKrst) {
			
			double [] adX;
			double fMoleH2OLad=0;
			Hashtable<Spezies, Double> molenBruecheHash=new Hashtable<Spezies, Double>(15);			
			
			double fO2min =cKrst+hKrst/4- oKrst/2;
			double fAtomeC = cKrst;
			double fAtomeH = hKrst;
			double fAtomeO = oKrst;
			double fAtomeN = nKrst;
			double fAtomeS = 0;
			
			double fVol_CO2Luft=PhysKonst.get_vol_CO2_Luft();
			double fVol_O2Luft=PhysKonst.get_vol_O2_Luft();
			double fVol_N2Luft=PhysKonst.get_vol_N2_Luft();
			double fVol_ArLuft = PhysKonst.get_vol_Ar_Luft();
			
			// Zusammensetzung der Reaktionspartner (Krst, Luft, zusaetzl. H2O)
			// Restgas wird nicht betrachtet:
			// ----------------------------------------------------------------
			// fO2min: stoechiometrischer Sauerstoffbedarf [molO2/molKrst]
			// dLambdaO2min: zugefuehrter Sauerstoff [molO2/molKrst]
			// Zusammensetzung der Luft:
			// ca. 21% Sauerstoff, ca. 78% Stickstoff, ca. 0.03% Kohlenioxid
			// Zusammensetzung des Kraftstoffs:
			// in den meisten Faellen Komposit aus C-H-O-(N)

			   
			   
			// dLambdaO2min=LambdaV*(AnzC-Atome + AnzH-Atome/4 + AnzO-Atome/2)des Krst (KOEF)
				double dLambdaO2min=lambda*fO2min;
			// C-Atome der Reaktanten (Kohlenstoffbilanz):
//					Kohlenstoffatome aus Krst + Kohlenstoff aus Luft
//			    AnzC-Atome aus Krst + Molenbruch CO2/O2*KOEF    
//			                                    (0.0014*KOEF)
			   double dAnzC=fAtomeC+dLambdaO2min*fVol_CO2Luft/fVol_O2Luft;
			// H-Atome der Reaktanten (Wasserstoffbilanz): 
//					Wasserstoff aus Krst + Wasserstoff aus feuchter Luft + Wasserstoff aus zusaetz. Wasser
//			    AnzH-Atome aus Krst + 2*Molenbruch H2O/Krst
				double dAnzH=fAtomeH+2*fMoleH2OLad;
			// O-Atome der Reaktanten (Sauerstoffbilanz):
//					Sauerstoff aus Krst + Sauerstoff aus O2-Gehalt der Luft + Sauerstoff aus CO2-Gehalt der Luft + Sauerstoff aus zusaetzl Wasser
//			    AnzO-Atome aus Krst + 2*KOEF*Molenbruch O2/O2 + 2*KOEF*Molenbruch CO2/O2 + Molenbruch H2O/Krst
//			                         (2*KOEF*1)                (2*KOEF*0.0014)
			   double dAnzO=fAtomeO+2*dLambdaO2min*(1+fVol_CO2Luft/fVol_O2Luft)+fMoleH2OLad;
			// N-Atome der Reaktanten (Stickstoffbilanz):
//					(Stickstoff aus Krst) + Stickstoff aus Luft
//					(AnzN-Atome aus Krst) + 2*KOEF*Molenbruch N2/O2
//			                           (2*KOEF*3.7274)		   
		
			   double dAnzN=fAtomeN+2*dLambdaO2min*fVol_N2Luft/fVol_O2Luft;
			   
// 				Ar-Atome de Reaktanten (Argonbilanz):
//				Argon der Luft
//				KOEF*Molenbruch Ar/O2
			   double dAnzAr=dLambdaO2min*fVol_ArLuft/fVol_O2Luft;
			
			
			
			   adX=calc_gg(p,T,lambda,dAnzC,dAnzH,dAnzO,dAnzN,dAnzAr);
			   
			   molenBruecheHash.put(spezH,adX[1]);
			   molenBruecheHash.put(spezO,adX[2]);
			   molenBruecheHash.put(spezN,adX[3]);
			   molenBruecheHash.put(spezH2,adX[4]);
			   molenBruecheHash.put(spezOH,adX[5]);
			   molenBruecheHash.put(spezCO,adX[6]);
			   molenBruecheHash.put(spezNO,adX[7]);
			   molenBruecheHash.put(spezO2,adX[8]);
			   molenBruecheHash.put(spezH2O,adX[9]);
			   molenBruecheHash.put(spezCO2,adX[10]);
			   molenBruecheHash.put(spezN2,adX[11]); 
//			   molenBruecheHash.put(spezAr,adX[12]);		
			
			   return molenBruecheHash;
		}


		@Override
		public Hashtable<Spezies, Double> get_GG_molenBrueche(double p,
				double T, Spezies frischGemisch) {
			Hashtable<Spezies, Double> molenBruecheHash=new Hashtable<Spezies, Double>(15);

			double [] adX;	
			double lambda=0,o2,n2,cKrst,hKrst;
			double dAnzC=cKrst=frischGemisch.get_AnzC_Atome();
			double dAnzH=hKrst=frischGemisch.get_AnzH_Atome();
			o2=0.5*frischGemisch.get_AnzO_Atome();
			n2=0.5*frischGemisch.get_AnzN_Atome();
			double dAnzO=o2/2;
			double dAnzN=n2/2;
			//TODO Argon beruecksichtigen
			double dAnzAr=0; //vielleicht kann man hier nochmal irgendwann eine saubere Lösung finden, um auch Ar zu berücksichtigen!
			//TODO Abschaetzung ueberarbeiten
//			ABSCHÄTZUNG des Lambda-Wertes aus der Zusammensetzung der Ladung
//			Wenn der Ladung beispielsweise Wasser beigemischt ist, wird 
//			dies behandelt wie eine WasserKraftstoffemulsion.
//			Dies gilt für alle Komponenten die c,h oder o-Atome enthalten
//			Berechnung erfolgt auf Basis der linken Seite der allgemeinen Verbrennungsgleichung
//			CxHyOz+LAM*(x+y/4-z2)*(O2+3,773N2)-->...
//			CO2 wird nicht berücksichtigt
			
			//hier genügt eigentlich schon die Abschätzung ob fette oder Magere Verbrennung vorliegt.
			//die Entscheidung kann auf Basis der Sauerstoffbedarfs getroffen werden 
			// O2min > null--> Fett 
			// O2min < null--> mager 
			
			double A=PhysKonst.get_vol_N2_Luft()/PhysKonst.get_vol_O2_Luft();
			lambda=n2/(A*(cKrst+hKrst/4+n2/A-o2));
			
			adX=calc_gg(p,T,lambda,dAnzC,dAnzH,dAnzO,dAnzN,dAnzAr);	

			molenBruecheHash.put(spezH,adX[1]);
			molenBruecheHash.put(spezO,adX[2]);
			molenBruecheHash.put(spezN,adX[3]);
			molenBruecheHash.put(spezH2,adX[4]);
			molenBruecheHash.put(spezOH,adX[5]);
			molenBruecheHash.put(spezCO,adX[6]);
			molenBruecheHash.put(spezNO,adX[7]);
			molenBruecheHash.put(spezO2,adX[8]);
			molenBruecheHash.put(spezH2O,adX[9]);
			molenBruecheHash.put(spezCO2,adX[10]);
			molenBruecheHash.put(spezN2,adX[11]);			


			return molenBruecheHash;
		}
		
		

	}
