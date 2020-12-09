# indoor-positioning-app

System przeznaczony do lokalizacji użytkownika telefonu mobilnego wewnątrz budynku.

### Opis projektu
Celem pracy jest projekt i implementacja oprogramowania zbierającego dane o natężeniu sygnałów WiFi i Bluetooth celem ustalenia pozycji urządzenia mobilnego (telefonu) użytkownika wewnątrz budynku.
Brane pod uwagę będą dwa przypadki wykorzystania urządzeń zewnętrznych wyposażonych w nadajniki WiFi lub Bluetooth: pasywne oraz aktywne.  
Pasywne polega na odczycie siły sygnału z innych urządzeń jedynie w urządzeniu mobilnym.  
Aktywne polega na odczycie siły sygnałów zarówno w urządzeniu mobilnym jak i w urządzeniach wyposażonych w nadajniki WiFi lub Bluetooth.  


### Opis działania

System składa się z kilku elementów:
* **aplikacja mobilna** - skanuje urządzenia emitujące sygnały Wifi i Bluetooth Low Energy, a także ewentualne dane z urządzeń ESP i na podstawie tych danych wyznacza pozycję użytkownika
* **mikrokontrolery ESP32** - posiadają kilka funkcji:
    * działają jako Access Pointy, dzięki czemu można zmierzyć siłę ich sygnałów
    * skanują dostępne urządzenia w okolicy i przesyłają te dane po HTTP do API webowego
* **web api** - serwer służący do analizy danych pozyskanych z ESP. Wylicza średnią referencyjną siłę sygnału do każdego urządzenia, którego siłę sygnału wykorzystujemy w aplikacji i w przypadku zmiany pozycji, dodaje to je do listy wykluczonych, aby nie były brane pod uwagę podczas wyznaczania lokalizacji. Dodatkowo w przypadku gdy użytkownik włączy w aplikacji hotspot, przekazuje dane o sile sygnału do telefonu z ESP.

### Działanie aplikacji mobilnej
#### Obszary

Podstawą działania aplikacji są obszary zdefiniowane przez użytkownika.  
Podczas dodawania obszarów, użytkownik przemieszcza się po pokoju, tak aby równomiernie go zeskanować.  
Dodatkowo, istnieje możliwość wybrania, których z dostępnych urządzeń chcemy użyć do lokalizowacji, a także nazwania obszaru.
Po zakończeniu skanowania wyliczana jest średnia siła sygnału i odchylenie standardowe do każdego urządzenia, a następnie dodawane te informacje dodawane są do bazy dancyh. W przypadku, gdy aplikacja działa w trybie aktywnym, a użytkownik włączył hotspot wifi, analogiczne parametry są wyliczane z danych dostępnych z ESP.
<p align="center">
  <img src="https://user-images.githubusercontent.com/33720728/101677846-6a04c880-3a5d-11eb-8383-5400b56c94f3.jpg" height="400"> </br>
  Widok dodawania obszaru
</p>

#### Wyznaczanie lokalizacji użytkownika

Po dodaniu obszarów i wciśnięciu przycisku START na ekranie głównym, w tle zaczyna się skanowanie. Po uzyskaniu wyników skanowana, analizowane są dane i wyznaczana jest najbardziej prawdopodobny obszar, w którym znajduje się użytkownik. Gdy jest to inny obszar, to aktualizowane zostaje powiadomienie. Dodatkowo w przypadku gdy otworzona jest aplikacja, to na głównym widoku pokazywane są szczegółowe wyniki analizy. 
<p align="center">
  <img src="https://user-images.githubusercontent.com/33720728/101677843-68d39b80-3a5d-11eb-900a-1958eef56002.jpg" height="400"> </br>
  Główny widok z wynikami z analizy
</p>

#### Algorytm wyznaczania lokalizacji użytkownika

Przy skanowaniu obszarów przyjmujemy, że rozkład siły sygnału do każdego urządzenia w obszarze jest zbliżony do rozkładu normalnego. Dzięki temu, że mamy informację o średniej sile sygnału i odchyleniu standardowym, używając wzoru na dystrybuantę, możemy wyznaczyć dla każdego urządzenia jak daleko od wartości oczekiwanej jest aktualna siła sygnału. W przypadku, gdy apliakcja działa w trybie aktywnym, pobierana z web api jest lista urządzeń, które zmieniły swoją pozycję, a także, siła sygnałów z ESP do naszego hotspotu w telefonie. Następnie wyliczamy, dla którego obszaru odchylenia te są najmniejsze i na tej podstawie wyznaczamy lokalizację użytkownika. 

#### Ustawiania

W ustawieniach istnieje możliwość zmiany trybu z pasywnego na aktywny, zmiany nazwy hotspotu, zmiany minimalnego dosasowania do obszaru (aby wiedzieć, kiedy nie znajdujemy się w żadnym obszarze), a także z jakiego czasu brana jest średnia zeskanowanych urządzeń (czym większy, tym większa dokładność, lecz dłużej zajmuje aktualizacja po przemieszczeniu się). Dodatkowo w ustawianiach można dodać lub usunąć obszary
<p align="center">
  <img src="https://user-images.githubusercontent.com/33720728/101677848-6a04c880-3a5d-11eb-85ee-68f1e811b6d1.jpg" height="400"> </br>
  Widok ustawień
</p>

### UWAGI

* Aplikacja została stworzona na urządzenia z Androidem 8.0 lub nowszym, lecz niestety Google wraz z kolejną wersją systemu wprowadza coraz większe restrykcje do używania WiFi i Bluetooth, przez co od wersji 9.0 Wifi można skanować jedynie 4 razy w ciągu 2 minut. W związku z tym aplikację najlepiej testować na Androidzie 8.0 lub 8.1, a w przypadku używania wyższych wersji, ustawić maksymalny wiek skanu na wartość większą niż 30000ms (30 sekund).

* Pod uwagę brane są jedynie urządzenia Bluetooth, które emitują sygnał Bluetooth Low Energy (większośc urządzeń stale udostęniających Bluetooth używa właśnie tej wersji).
