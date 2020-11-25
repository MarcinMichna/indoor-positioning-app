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

### Zasada działania

System składa się z kilku elementów:
* **aplikacja mobilna** - ma za zadanie wyznaczyć w jakim obszarze znajduje sie użytkownik na podstawie siły sygnałów wifi i bluetooth, a także opcjonalnych danych z ESP32. 
    * użytkownik dodaje swoje obszary w aplikacji 
    * podczas dodawania obszaru użytkownik przemieszcza się po obszarze, tak, aby aplikacja była w stanie uzyskać siły sygnałów w tych miejscach
    * wyliczane są zakresy sił sygnałów do każdego urządzenia w jakich uznaje się, że użytkownik znajduje się w danej lokalizacji. Jest to wersja próbna, aby sprawdzić na ile dokładne jest to rozwiązanie. Gdy nie będzie żadnych technologicznych problemów do tej pory, zajmę się stworzeniem dokładniejszego i jak najbardziej optymalnego pod względem zużycia energii algorytmem.
    * dzięki danym z ESP32 które z założenia nie poruszają się, jesteśmy w stanie stwierdzić, gdy jakieś inne urządzenie, którego używamy, zmieniło swoją lokalizację, a także przydadzą się przy bardziej zaawansowanym algorytmie lokalizacji.
* **mikrokontrolery ESP32** - posiadają kilka funkcji:
    * działają jako Access Pointy dzięki czemu można zmierzyć siłę ich sygnałów
    * skanują dostępne urządzenia w okolicy i analizują, czy urządzenia nie zmieniły swojego położenia
* **web api** - serwer służący do przekazania danych z ESP32 do aplikacji. Mimo, że wydaje się to nieoptymalnym rozwiązaniem ze względu na czas jaki zajmie przekazanie danych i wymaga ciągłego połączenia z internetem, wydaje mi się to najlepszą opcją. Alternatywą byłoby zrobienie takiego serwera lokalnie na jednym z ESP32, lub łączenie się z nimi bezpośrednio, lecz oznaczałoby to większe zużycie energii, a także nie byłoby wygodne, ponieważ ograniczałoby to używanie modułu BT do innych celów. Co więcej, ciągłe połączenie z internetem jest już raczej standardem w obecnych czasach. Dodatkowo analiza zmiany położenia urządzeń których siłę sygnału mierzymy, mogłaby się tutaj odbywać.
<p align="center">
  <img src="https://user-images.githubusercontent.com/33720728/97166298-52130700-1785-11eb-9a1d-84fc14fbce3d.png"> </br>
  Poglądowy schemat systemu
</p>
