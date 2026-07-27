# LEARNING_GUIDE.md — Kako pročitati i naučiti cijeli LedVision projekt

> Ovo nije referenca da je "pregledaš" — ovo je redoslijed po kojem **otvaraš fajlove u IntelliJ-u i čitaš ih naglas sam sebi**, objašnjavajući svaku liniju kao da učiš nekog drugog. Ako u nekom koraku ne možeš objasniti zašto nešto postoji, vrati se, pročitaj komentare u tom fajlu (namjerno su tu, ostavljeni upravo za ovo), i probaj ponovno prije nego kreneš dalje.
>
> Svaki korak ima: **koje fajlove otvoriti**, **na što obratiti pažnju**, i **pitanje za provjeru** da testiraš jesi li stvarno shvatio.

---

## Korak 0 — Pokreni aplikaciju dok čitaš

Imaj `LedVisionApplication` pokrenutu u IntelliJ-u i `http://localhost:8081` otvoren u drugom prozoru cijelo vrijeme dok prolaziš ovaj guide. Svaki puta kad pročitaš nešto u kodu, **odmah to isprobaj u browseru** — to je najbrži način da spojiš kod s onim što stvarno vidiš.

---

## Korak 1 — Ulazna točka i konfiguracija (5 min)

**Otvori:**
- `src/main/java/hr/algebra/ledvision/LedVisionApplication.java`
- `pom.xml`
- `src/main/resources/application.properties`

**Na što gledati:**
- `LedVisionApplication.java` — `@SpringBootApplication` je jedna anotacija koja uključuje auto-konfiguraciju, component scan i Spring Boot bootstrap. `main()` metoda samo poziva `SpringApplication.run()` — sve ostalo (kreiranje svih Bean-ova, pokretanje Tomcata, spajanje na bazu) Spring radi automatski iza scene.
- `pom.xml` — pogledaj popis `<dependency>` blokova. Svaki `spring-boot-starter-*` je paket ovisnosti za jednu funkcionalnost (web, security, data-jpa, thymeleaf...). `jjwt-*` je JWT biblioteka, `rest-api-sdk` je PayPal.
- `application.properties` — ovo je "single source of truth" za konfiguraciju. Pogledaj `${DATABASE_URL}` sintaksu — to znači "uzmi vrijednost iz env varijable", što je upravo ono što si postavljao u IntelliJ Run Configuration.

**Pitanje za provjeru:** Zašto `spring.datasource.password` u fajlu piše `${DATABASE_PASSWORD}` umjesto stvarne lozinke?
*(Odgovor: da se lozinka nikad ne commita u git — svatko tko pokreće projekt postavlja svoju vlastitu env varijablu.)*

---

## Korak 2 — Domenski model, redoslijedom ovisnosti (20 min)

Ovo je **najvažniji korak**. Čitaj ovim točnim redoslijedom jer svaki sljedeći entitet ovisi o prethodnom:

1. `model/User.java` — korisnik, s `Role` enumom (BUYER/ADMIN). Svaki drugi entitet na kraju vodi natrag do korisnika.
2. `model/Location.java` — fizička lokacija u Zagrebu. Pročitaj komentar na vrhu — objašnjava da je ovo bio `Category` prije refaktora.
3. `model/AdSpacePackage.java` — paket oglasnog prostora, `@ManyToOne` prema `Location`. Pročitaj komentar — objašnjava zašto `price`/`stockQuantity` više NE postoje ovdje.
4. `model/PricingTier.java` — cjenovna opcija (trajanje + veličina + cijena), `@ManyToOne` prema `AdSpacePackage`. Ovo je entitet koji **stvarno nosi cijenu**.
5. `model/AdExample.java` — galerija slika/videa po paketu, `mediaType` enum (IMAGE/VIDEO).
6. `model/Order.java` — narudžba/rezervacija, `@ManyToOne` prema `User`, `@OneToMany` prema `OrderItem`. Pogledaj `OrderStatus` i `PaymentMethod` enume.
7. `model/OrderItem.java` — jedna linija narudžbe, `@ManyToOne` prema `PricingTier` (ne prema `AdSpacePackage` direktno!). Pročitaj komentar — objašnjava `priceAtPurchase` snapshot logiku.
8. `model/RefreshToken.java`, `model/LoginHistory.java` — sporedni entiteti za JWT refresh i audit log prijava.

**Nacrtaj na papiru** (ili u glavi) ovaj dijagram odnosa dok čitaš:
```
User 1---* Order 1---* OrderItem *---1 PricingTier *---1 AdSpacePackage *---1 Location
User 1---* RefreshToken
User 1---* LoginHistory
AdSpacePackage 1---* AdExample
```

**Pitanje za provjeru:** Zašto `OrderItem` ne referencira `AdSpacePackage` direktno, nego ide kroz `PricingTier`?
*(Odgovor: jer cijena i "što je točno kupljeno" — trajanje/veličina — žive na tier-u, ne na paketu. Paket sam po sebi nema cijenu.)*

---

## Korak 3 — Repositoriji: kako Spring razgovara s bazom (15 min)

**Otvori:**
- `repository/LocationRepository.java` (najjednostavniji — samo `extends JpaRepository`)
- `repository/AdSpacePackageRepository.java` (ima `@Query` s `JOIN FETCH`)
- `repository/OrderRepository.java` (najsloženiji — više `@Query` metoda)

**Na što gledati:**
- `JpaRepository<AdSpacePackage, Long>` ti besplatno daje `save()`, `findById()`, `findAll()`, `deleteById()` — nisi ih ti pisao, Spring Data ih generira iz interfacea.
- `@Query("SELECT p FROM AdSpacePackage p JOIN FETCH p.location WHERE p.id = :id")` — ovo je JPQL (Java Persistence Query Language), sličan SQL-u ali radi nad Java objektima/poljima, ne nad tablicama/stupcima. `JOIN FETCH` znači "odmah učitaj i povezani entitet u istom upitu" (bez toga bi `location` bio `null` dok ga eksplicitno ne dohvatiš — tzv. lazy loading problem).
- `OrderRepository` ima `findByUserIdAndCreatedAtBetween` i `findByCreatedAtBetween` — to su metode čija imena Spring **sam parsira** i generira SQL iz njih (nema `@Query` anotacije). Ovo su metode koje trenutno **postoje ali se nigdje ne pozivaju** — to je poznata rupa u funkcionalnosti o kojoj smo pričali (admin filter po kupcu/periodu).

**Pitanje za provjeru:** Što bi se dogodilo da u `findByIdWithLocation` upitu makneš `JOIN FETCH` i ostaviš samo `WHERE p.id = :id`?
*(Odgovor: upit bi i dalje radio, ali `location` polje na vraćenom objektu bi moglo baciti grešku ili biti prazno kad ga template pokuša ispisati, ovisno o trenutku kad se sesija zatvori — klasičan "LazyInitializationException" rizik.)*

---

## Korak 4 — DTO-ovi i mapperi: zašto ne šaljemo entitete direktno (10 min)

**Otvori:**
- `dto/AdSpacePackageDto.java`
- `mapper/AdSpacePackageMapper.java`
- `dto/LocationDto.java` + `mapper/LocationMapper.java`

**Na što gledati:**
- DTO (Data Transfer Object) ima **manje polja** nego entitet i nema JPA anotacije. Zašto? Jer entitet (`AdSpacePackage`) ima `@ManyToOne Location location` — kad bi to direktno pretvorio u JSON, dobio bi beskonačnu petlju ili prevelik/nekontroliran odgovor (cijeli Location objekt, koji možda ima svoje reference natrag...).
- Mapper je čista funkcija: `toDto(entitet) → dto` i `toEntity(dto) → entitet`. Nema logike, samo prepisivanje polja.
- Primijeti: MVC kontroleri (Thymeleaf) rade **direktno s entitetima** (`AdSpacePackageService` vraća `AdSpacePackage`), dok REST API kontroleri rade s **DTO-ovima** (`AdSpacePackageApiController` vraća `AdSpacePackageDto`). To je namjerna razlika — template može sigurno čitati lazy-loaded entitet unutar iste HTTP requesta (Thymeleaf renderira u istoj transakciji), dok JSON serializacija za vanjski API klijent to ne smije raditi.

**Pitanje za provjeru:** Zašto `AdSpacePackageDto` ima `locationId` i `locationName` kao dva odvojena `Long`/`String` polja, umjesto jednog `Location location` polja?
*(Odgovor: da JSON ostane plosnat i jednostavan za klijenta — ne treba mu cijeli Location objekt, samo ime i ID za prikaz/link.)*

---

## Korak 5 — Servisi: poslovna logika (20 min)

**Otvori ovim redoslijedom:**
1. `service/AdSpacePackageService.java` — CRUD za pakete + lokacije (entiteti, za MVC)
2. `service/LocationService.java` — CRUD za lokacije (DTO-ovi, za API) — usporedi s #1, primijeti dupliciranu ulogu ali za druge slojeve
3. `service/PricingTierService.java`, `service/AdExampleService.java` — mali servisi, svaki radi jednu stvar
4. `service/CartService.java` — pažljivo pročitaj komentar na vrhu. Košarica je `Map<Long tierId, Integer quantity>` spremljena u HTTP sesiju (ne u bazi!)
5. `service/OrderService.java` — `createOrder()` metoda je srce cijele kupovine. Prati liniju po liniju: uzima korisnika, prolazi kroz stavke košarice, za svaku dohvaća `PricingTier`, računa ukupnu cijenu, sprema `OrderItem`.

**Pitanje za provjeru:** Zašto je košarica spremljena u `HttpSession` a ne u bazi podataka?
*(Odgovor: jer anonimni korisnici (bez prijave) prema specifikaciji projekta smiju imati košaricu — nema `User` na kojeg bi je vezao dok se ne prijavi tek na checkoutu.)*

---

## Korak 6 — Sigurnost i JWT: prati cijeli tok prijave (30 min — najteži dio)

Ovo je dio koji većina studenata najviše muči. Idi polako.

**Otvori ovim redoslijedom:**

1. `security/SecurityConfig.java` — **dva odvojena filter chain-a**:
   - `apiFilterChain` (`@Order(1)`, `securityMatcher("/api/**")`) — bez sesije (`STATELESS`), štiti REST API JWT tokenom
   - `webFilterChain` (`@Order(2)`, sve ostalo) — klasična sesijska prijava s formom (`formLogin`)

   Ovo su **dva potpuno različita mehanizma autentikacije koji rade usporedno u istoj aplikaciji** — jedan za browser (Thymeleaf stranice), jedan za API klijente (Postman, mobilna app, itd).

2. `security/JwtService.java` — generira i validira JWT tokene. Pogledaj `generateAccessToken`/`generateRefreshToken` — različito trajanje (`jwt.expiration.access` = 15 min, `jwt.expiration.refresh` = 7 dana, iz `application.properties`).

3. `filter/JwtAuthFilter.java` — pokreće se na **svaki** HTTP request (jer je `@Component`, Spring Boot ga auto-registrira globalno — sjeti se NPE bug-a koji smo popravili, upravo zbog ovog globalnog dosega). Provjerava `Authorization: Bearer <token>` header; ako je valjan, ručno postavlja `Authentication` u `SecurityContextHolder`.

4. `security/UserDetailsServiceImpl.java` + `security/CustomUserDetails.java` — Spring Security ne zna ništa o tvom `User` entitetu; `CustomUserDetails` je "adapter" koji ga omata u sučelje (`UserDetails`) koje Spring razumije.

5. `controller/api/AuthController.java` + `service/AuthService.java` — `/api/auth/register`, `/api/auth/login`, `/api/auth/refresh`. Ovo je mjesto gdje se access+refresh tokeni **prvi put izdaju**.

6. `security/CustomAuthSuccessHandler.java` — poziva se nakon uspješne **sesijske** (web forme) prijave; kreira refresh token i postavlja ga kao cookie (`CookieHelper`).

**Pitanje za provjeru:** Ako obrišeš `Authorization` header iz zahtjeva prema `/api/packages` (GET), hoće li zahtjev i dalje raditi?
*(Odgovor: da — pogledaj `SecurityConfig`, GET na `/api/packages/**` je eksplicitno `permitAll()`. Probaj isto s POST-om na isti endpoint — to bi trebalo tražiti autentikaciju.)*

---

## Korak 7 — Filteri i slušači (obavezni dio specifikacije) (10 min)

**Otvori:**
- `filter/JwtAuthFilter.java` (već si ga vidio — ovo je "filter" iz specifikacije, I5)
- `listener/AuthenticationSuccessListener.java` (I6 — "listener")
- `listener/SessionListener.java`
- `service/AsyncService.java`

**Na što gledati:**
- `AuthenticationSuccessListener` implementira `ApplicationListener<AuthenticationSuccessEvent>` — Spring Security **automatski** emitira taj event nakon svake uspješne prijave, listener ga "sluša" i reagira (bilježi login history, poziva `AsyncService` da to napravi **asinkrono** — ne blokira response korisniku dok se piše u bazu).
- `SessionListener` implementira `HttpSessionListener` — servlet-standardni listener, javlja se kad se sesija kreira/uništi.
- `AsyncService` metoda ima `@Async` anotaciju — to je "implementacija asinkronih funkcionalnosti" iz specifikacije. Provjeri je li `@EnableAsync` negdje omogućen (potraži u `LedVisionApplication.java` ili nekom `@Configuration` fajlu).

**Pitanje za provjeru:** Zašto se bilježenje login historyja radi asinkrono umjesto sinkrono?
*(Odgovor: da pisanje u bazu za audit log ne uspori/blokira samu prijavu korisnika — korisnik dobije odgovor odmah, log se upiše "u pozadini".)*

---

## Korak 8 — Prati jedan cijeli korisnički put kroz MVC kontrolere (25 min)

Ovo je najkorisniji korak za razumijevanje "kako sve sjeda skupa". Otvori browser na `/` i **paralelno** prati kod za svaki klik:

1. **Home page** (`GET /`) → `controller/mvc/AdSpacePackageViewController.home()` → `templates/index.html`
2. **Klik na lokaciju** → `GET /packages?locationId=X` → `AdSpacePackageViewController.packages()` → `templates/packages/list.html`
3. **Klik na paket** → `GET /packages/{id}` → `AdSpacePackageViewController.packageDetail()` — primijeti da poziva **tri servisa** (package, tiers, examples) da sastavi jednu stranicu → `templates/packages/detail.html`
4. **Klik "Add to Cart" na tier-u** → JavaScript (`static/js/cart.js` `addToCart()`) → `fetch POST /api/cart/add` → `controller/api/CartApiController.addToCart()` → `service/CartService.addToCart()`
5. **Odlazak na `/cart`** → `controller/mvc/CartViewController.cart()` → `templates/cart/cart.html`
6. **Checkout** → `controller/mvc/OrderViewController.checkout()` (GET, prikaz forme) pa `placeOrder()` (POST, stvarna kupnja) → `service/OrderService.createOrder()`
7. **Pregled narudžbe** → `orderDetail()` → `templates/orders/detail.html`

**Pitanje za provjeru:** Zašto `packageDetail()` metoda poziva tri odvojena servisa umjesto da je sve u jednom?
*(Odgovor: svaki servis je odgovoran za jedan entitet — Single Responsibility. Kontroler ih sastavlja zajedno za tu specifičnu stranicu, ali servisi ostaju međusobno neovisni i ponovno iskoristivi.)*

---

## Korak 9 — REST API i Swagger (10 min)

**Otvori:**
- `config/OpenApiConfig.java`
- Bilo koji `controller/api/*.java`

**Isprobaj u browseru:** `http://localhost:8081/swagger-ui.html` — ovo je **automatski generirana** interaktivna dokumentacija svih tvojih REST endpointova, izvučena iz anotacija (`@Operation`, `@Tag`) u kontrolerima. Klikni "Try it out" na nekom GET endpointu i pošalji ga direktno iz browsera.

**Pitanje za provjeru:** Zašto `LocationApiController.create()` ima `@PreAuthorize("hasRole('ADMIN')")` a `getAllLocations()` nema ništa?
*(Odgovor: čitanje lokacija je javno (svatko smije vidjeti ponudu), ali kreiranje mijenja podatke pa smije samo admin — to je autorizacija na razini metode, dodatni sloj povrh `SecurityConfig`-a.)*

---

## Korak 10 — Admin CRUD tok (15 min)

Ovo si već radio ručno kroz browser — sad pročitaj kod iza toga:

- `controller/mvc/AdminViewController.java` — Location i AdSpacePackage CRUD, Orders pregled, Login History
- `controller/mvc/AdminPackageMediaController.java` — PricingTier i AdExample CRUD (namjerno odvojeno u drugi fajl da nijedna klasa ne pređe 200 linija)

**Na što gledati:** `savePackage()` metoda prima `id` kao `@RequestParam(required = false)` — ako je `null`, znači da je ovo NOVI paket (kreira se `new AdSpacePackage()`); ako postoji, dohvaća postojeći i mijenja mu polja. Ovo je čest pattern za "jedna forma za create i edit".

**Pitanje za provjeru:** Zašto `newTierForm()` treba `packageId` kao path varijablu, dok `editTierForm()` treba samo `id` tier-a?
*(Odgovor: novi tier još nema svoj ID, pa treba znati kojem paketu pripada da ga poveže; postojeći tier već ima `AdSpacePackage` referencu spremljenu, pa mu ne treba dodatni parametar.)*

---

## Korak 11 — Templates (Thymeleaf) (15 min)

**Otvori:**
- `templates/fragments/head.html`, `templates/fragments/navbar.html` — dijeljeni fragmenti, uključeni u svaku stranicu preko `th:replace`
- `templates/packages/detail.html` — pogledaj `th:each="tier : ${tiers}"` (petlja) i `th:if="${pkg == null}"` (uvjet)

**Na što gledati:** `${pkg.location.name}` u Thymeleaf izrazu direktno navigira kroz JPA relaciju (`pkg` → `location` → `name`) unutar HTML-a, bez ijedne linije Java koda za to specifično polje. To je moguće samo zato što je `location` bio `JOIN FETCH`-an u repository upitu (Korak 3) — inače bi ovo bacilo grešku.

**Pitanje za provjeru:** Što se dogodi ako `AdSpacePackageService.getPackageById()` koristi obični `findById()` umjesto `findByIdWithLocation()`, a template i dalje pokušava ispisati `${pkg.location.name}`?
*(Odgovor: vjerojatno `LazyInitializationException`, jer JPA sesija koja bi mogla lijeno dohvatiti `location` je zatvorena prije nego Thymeleaf pokuša pristupiti tom polju.)*

---

## Korak 12 — Vježbe da stvarno zapamtiš (napravi barem 3)

1. Dodaj novo polje `screenResolution` (String) na `AdSpacePackage` — provuci ga kroz entitet → DTO → mapper → admin formu → detail stranicu. Ovo ti pokazuje **cijeli vertikalni presjek** aplikacije.
2. U `packages/list.html`, dodaj prikaz broja dostupnih tier-ova po paketu (trebat ćeš proslijediti tu informaciju iz kontrolera).
3. Namjerno pokvari nešto (npr. obriši `JOIN FETCH` iz jednog upita) i promatraj **točno** koju grešku i gdje dobiješ — najbrži način da naučiš čitati stack traceove.
4. Implementiraj onaj nedostajući admin filter (po kupcu + periodu) iz Koraka 3 — to ti je i realan zadatak za dovršiti specifikaciju.

---

## Kako ovo objasniti na prezentaciji (brzi cheat-sheet)

- **I1 (Spring MVC):** svi `controller/mvc/*ViewController.java`
- **I2+I3 (Thymeleaf):** `templates/**/*.html`, `th:` atributi
- **I4 (Spring Security):** `security/SecurityConfig.java`, dva filter chaina
- **I5 (Filter):** `filter/JwtAuthFilter.java`
- **I6 (Listener):** `listener/AuthenticationSuccessListener.java`, `listener/SessionListener.java`
- **JWT access+refresh:** `security/JwtService.java`, `controller/api/AuthController.java`, `model/RefreshToken.java`
- **Async:** `service/AsyncService.java`, pozvan iz `AuthenticationSuccessListener`
- **≤200 linija/klasa:** provjereno, npr. `AdminPackageMediaController` odvojen baš zbog ovog pravila
- **Domenska tema:** LED oglasni prostor u Zagrebu — `PLAN.md` u rootu ima cijelu povijest refaktora ako te pitaju "kako je nastao ovaj projekt"
