# indoor-positioning-app

System przeznaczony do lokalizacji użytkownika wewnątrz budynku.

### Opis
Celem pracy jest projekt i implementacja oprogramowania zbierającego dane o natężeniu sygnałów WiFi i Bluetooth celem ustalenia pozycji urządzenia mobilnego (telefonu) użytkownika wewnątrz budynku.
Brane pod uwagę będą dwa przypadki wykorzystania urządzeń zewnętrznych wyposażonych w nadajniki WiFi lub Bluetooth: pasywne oraz aktywne.
Pasywne polega na odczycie siły sygnału z innych urządzeń jedynie w urządzeniu mobilnym.
Aktywne polega na odczycie siły sygnałów zarówno w urządzeniu mobilnym jak i w urządzeniach wyposażonych w nadajniki WiFi lub Blootooth.

### Zasada działania

System składa się z kilku elementów:
* **aplikacja mobilna** - ma za zadanie wyznaczyć w jakim obszarze znajduje sie użytkownik na podstawie siły sygnałów wifi i bluetooth, a także opcjonalnych danych z ESP32. 
    * użytkownik dodaje swoje obszary w aplikacji 
      * podczas dodawania obszaru użytkownik przemieszcza się po obszarze, tak, aby aplikacja była w stanie uzyskać siły sygnałów w tych miejscach
    * wyliczane są zakresy sił sygnałów do każdego urządzenia w jakich uznaje się, że użytkownik znajduje się w danej lokalizacji. Jest to wersja próbna, aby sprawdzić na ile dokładne jest to rozwiązanie. Gdy nie będzie żadnych technologicznych problemów do tej pory, zajmę się stworzeniem dokładniejszego i jak najbardziej optymalnego pod względem zużycia energii algorytmem.
    * dzięki danym z ESP32 które z założenia nie poruszają się, jesteśmy w stanie stwierdzić, gdy jakieś inne urządzenie, którego używamy, zmieniło swoją lokalizację, a także przydadzą się przy bardziej zaawansowanym algorytmie lokalizacji.
* **mikrokontrolery ESP32** - posiadają kilka funkcji:
    * działają jako Access Pointy dzięki czemu można zmierzyć siłę ich sygnałów
    * skanują dostępne urządzenia w okolicy i wysyłają informacje o nich do aplikacji (poprzez serwer)
* **web api** - serwer służący do przekazania danych z ESP32 do aplikacji. Mimo, że wydaje się to nieoptymalnym rozwiązaniem ze względu na czas jaki zajmie przekazanie danych i wymaga ciągłego połączenia z internetem, wydaje mi się to najlepszą opcją. Alternatywą byłoby zrobienie takiego serwera lokalnie na jednym z ESP32, lub łączenie się z nimi bezpośrednio, lecz oznaczałoby to większe zużycie energii, a także nie byłoby wygodne, ponieważ ograniczałoby to używanie modułu BT do innych celów. Co więcej, ciągłe połączenie z internetem jest już raczej standardem w obecnych czasach.  
<p align="center">
  <img src="https://user-images.githubusercontent.com/33720728/97166298-52130700-1785-11eb-9a1d-84fc14fbce3d.png"> </br>
  Poglądowy schemat systemu
</p>

### Obecny stan pracy
* gotowa wstępna wersja oprogramowania do ESP32 
* gotowy serwer przekazujący dane z ESP32 do aplikacji jako json
* podstawowa wersja aplikacji  
    &#9745; stworzenie projektu  
    &#9745; odbieranie danych z ESP32  
    &#9745; skanowanie sieci wifi  
    &#9744; skanowanie BT  
    &#9744; preprocessing danych  
    &#9744; widok do dodawania obszaru  
    &#9744; dodanie obszaru i jego konfiguracja  
    &#9744; serwis analizujący na bieżąco lokalizację  

