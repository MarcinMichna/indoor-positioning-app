# indoor-positioning-app

System przeznaczony do lokalizacji użytkownika wewnątrz budynku.

### Opis
Celem pracy jest projekt i implementacja oprogramowania zbierającego dane o natężeniu sygnałów WiFi i Bluetooth celem ustalenia pozycji urządzenia mobilnego (telefonu) użytkownika wewnątrz budynku.
Brane pod uwagę będą dwa przypadki wykorzystania urządzeń zewnętrznych wyposażonych w nadajniki WiFi lub Bluetooth: pasywne oraz aktywne.  
Pasywne polega na odczycie siły sygnału z innych urządzeń jedynie w urządzeniu mobilnym.  
Aktywne polega na odczycie siły sygnałów zarówno w urządzeniu mobilnym jak i w urządzeniach wyposażonych w nadajniki WiFi lub Bluetooth.  

### Aktualne działanie aplikacji
Aplikacja umożliwia definiowanie własnych obszarów. Podczas dodawania obszaru, użytkownik wybiera z listy, siła których sygnałów będzie używana podczas działania aplikacji.
W podglądzie pojawia się informacja o aktualnym zakresie siły sygnału. Podczas dodawania obszaru, użytkownik powinien się po nim przemieszczać.  

Po wciśnięciu przycisku START na ekranie głównym aplikacji, w tle rozpoczyna się skanowanie urządzeń WiFi i Bluetooth Low Energy. Następnie wyliczana jest średnia siła sygnału dla każdego urządzenia z kilku ostatnich sekund i dla każdego obszaru zlicza się ilość urządzeń, które znajdują się w zakresie siły sygnału każdego obszaru. Obszar z największą liczbą pasujących urządzeń jest obszarem, w którym się znajdujemy.

<p float="left">
   <img src="https://user-images.githubusercontent.com/33720728/100105647-e5fcef00-2e67-11eb-8491-d728493d46bd.jpg"  height="300">
   <img src="https://user-images.githubusercontent.com/33720728/100105639-e39a9500-2e67-11eb-8152-7c3f23705304.jpg"  height="300">
   <img src="https://user-images.githubusercontent.com/33720728/100110351-20b55600-2e6d-11eb-842e-8882402a1384.jpg"  height="300">
</p>

#### Dokładność

W sutuacji przedstawionej na poniższym schemacie (4 obszary obok siebie, urządzenia rozłożone w miarę równomiernie, zajdujemy się w jednym z nich - zaznaczone gwiazdką), po analizie opisanej wyżej, aplikacja pokazała, że ilość pasujących zasięgów dla poszczególnych obszarów wynosi:
sypialnia: 6  
łazienka: 5  
kuchnia: 5  
buiro: 8  
Jak widać, biuro nie ma znaczącej przewagi. Często zdaża się, że liczba pasujących przedziałów jest większa dla obszaru znajdującego się obok.

<img src="https://user-images.githubusercontent.com/33720728/100122325-890a3480-2e79-11eb-86c8-cc08fae516f5.png"  height="300">
