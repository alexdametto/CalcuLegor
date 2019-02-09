# CalcuLegor
CalcuLegor è un progetto di Ingegneria del Software [CT0090], Ca' Foscari, svolto da:
- Bottolo Beatrice 864676
- Dametto Alex 864094
- Lucenti Alessio 864459
- Tomaiuolo Stefano 864784
- Zilio Silvia 864988

Consiste in un applicazione Android che comunica con un robot Lego Mindstorm EV3 via Bluetooth.

## Introduzione

CalcuLegor si propone come un sistema educativo in grado di risolvere espressioni e, utilizzando un Lego EV3 MindStorm, stamperà la risoluzione passo passo.

## Installazione
### Lego MindStorm EV3

Per l'utilizzo del nostro sistema è necessaria l'installazione di LeJOS. Le istruzioni riguardo all'installazione si possono trovare sul [sito ufficiale](http://www.lejos.org/).
leJOS è un firmware sostitutivo che include una Java Virtual Machine (JVM) per l'esercuzione di programmi java. Tramite questi programmi è possibile comandare i motori ed i sensori del robot.
Abbiamo scelto questo firmware in quanto è molto semplice da utilizzare, permette di interfacciarsi facilmente con il bluetooth e perchè per il nostro progetto utilizziamo una libreria java per la risoluzione passo passo delle espressioni matematiche, "AnyMathLibrary".
La libreria è consultabile e gratuitamente scaricabile a questo [link](https://github.com/DaMeX97/AnyMathLibrary).

Il sorgente da noi proposto per il dispositivo EV3 si trova sotto la directory "ev3". Per l'esecuzione del sorgente controllare che sia inclusa la libreria "AnymathLibrary.jar".

### Applicazione Android (Versione 6.0+)

L'applicazione android è scaricabile gratuitamente dal Google Play Store a questo [link](https://play.google.com/store/apps/details?id=com.bdltz.calculegor&hl=it). I dispositivi supportati sono quelli con versione di Android Marshmallow (6.0) o suvvessivi.
