# PLAN.md — Refaktor: Talaria (e-commerce dijelovi za motore) → LedVision (rezervacija LED oglasnog prostora u Zagrebu)

> Ovaj dokument prati refaktor domene aplikacije. Svaki korak ima status (⏳ čeka / 🔄 u tijeku / ✅ gotovo) i radi se **jedan korak po commit**. Nakon svakog koraka pokreće se `mvnw clean compile` kao brza provjera. Puni `mvnw clean install` (s testovima) ostavljen je za kraj, jer `@SpringBootTest` diže cijeli Spring kontekst i treba spojenu bazu + env varijable.
>
> Koraci označeni **[MANUAL]** zahtijevaju akciju izvan mog dosega (ti to radiš na svom sustavu) — kod njih ću stati i čekati potvrdu prije nastavka, **tek kad na njih dođemo na red** (ne pitam unaprijed).
>
> **Konvencija komentara:** kroz cijeli refaktor dodajem komentare u kod koji kronološki i razumljivo objašnjavaju što se događa i zašto (na razini klase i na ključnim metodama), da cijeli projekt možeš pratiti i razumjeti bez da moraš sve sam rekonstruirati. Ovo je odstupanje od uobičajene prakse "piši kod bez komentara" — ovdje je eksplicitno traženo jer je ovo studentski projekt koji moraš moći objasniti/braniti.
>
> **Princip minimalne izmjene:** gdje god postojeći Talaria kod već radi dobro (JWT auth, PayPal, filteri, listeneri, Order/User model, admin CRUD obrazac, mapperi, DTO uzorak), taj se **obrazac ponovno koristi**, samo se preimenuje/prilagođava novoj domeni — ne piše se ništa iznova ako već postoji dobar uzorak u kodu.

---

## Odluke već donesene (kroz razgovor)

- **Naziv brenda:** LedVision
- **Java package:** `hr.algebra.talaria` → `hr.algebra.ledvision` (rename ide zajedno s domenskim refaktorom)
- **Seed podaci:** nema ih — admin panel (postojeći CRUD) koristi se za ručni unos pravih zagrebačkih lokacija/paketa nakon što se app spoji na bazu
- **Slike:** aplikacija već sprema samo `imageUrl` (string) — ili eksterni URL (kao trenutni Talaria hero image s Cloudflare R2) ili lokalna putanja `/images/...`. `/images/**` je već dopušten u `SecurityConfig` (web filter chain), pa lokalno posluživanje radi bez ikakvih izmjena koda — samo treba folder u `static/images/` i putanju upisati u odgovarajuće admin formu. Točne foldere navodim u Fazi 7.

## Entitetsko mapiranje (stara → nova domena)

| Staro | Novo | Napomena |
|---|---|---|
| `Category` | `Location` | LED lokacija u Zagrebu (naziv, adresa/opis, slika) |
| `Product` | `AdSpacePackage` | Paket oglasnog prostora vezan uz jednu `Location`; gubi `price`/`stockQuantity` |
| *(novo)* | `PricingTier` | Cjenovna opcija unutar paketa (trajanje, veličina, cijena) — 1:N na `AdSpacePackage` |
| *(novo)* | `AdExample` | Galerija primjera reklama po paketu (imageUrl, caption) — 1:N na `AdSpacePackage` |
| `OrderItem.product` | `OrderItem.pricingTier` | Rezervacija sada referencira konkretan tier, ne "proizvod" |
| `Order`, `User`, `RefreshToken`, `LoginHistory` | *(bez izmjena)* | Infrastruktura (auth, narudžbe, plaćanje) ostaje ista |

**Napomena o dosegu:** ovo NE implementira kalendarsko provjeravanje preklapanja termina (npr. da dvije rezervacije ne mogu zauzeti isti ekran u isto vrijeme) — to bi bila velika nova funkcionalnost, izvan "zamijeni domenske entitete" opsega koji si tražio. `quantity` na `OrderItem` ostaje kao broj rezerviranih jedinica tier-a (npr. 2× "1 mjesec" = 2 mjeseca), analogno kako je prije predstavljao količinu proizvoda.

---

## FAZA 0 — Priprema

### Korak 0.1 — [MANUAL] Environment varijable
**Status:** ⏳ čeka

Aplikacija čita ove env varijable (`application.properties`) i **ne treba ih za `mvn compile`**, ali treba za pokretanje aplikacije i za `mvn install` (testovi dižu Spring kontekst):

```
DATABASE_URL=jdbc:postgresql://<host>:<port>/<db_name>
DATABASE_USERNAME=<korisnik>
DATABASE_PASSWORD=<lozinka>
JWT_SECRET=<dugačak nasumičan string, min. 32 znaka>
PAYPAL_CLIENT_ID=<iz PayPal developer sandbox accounta>
PAYPAL_CLIENT_SECRET=<iz PayPal developer sandbox accounta>
```

Trebaš:
1. Imati pokrenut PostgreSQL (lokalno ili npr. Docker/Neon/Supabase) i kreiranu praznu bazu.
2. Postaviti ove varijable kao env varijable na sustavu, ili u IntelliJ Run Configuration (Environment variables polje) — **ne** commitati ih u repo (`.gitignore` već isključuje `.env`/`application-local.properties`).
3. Javiti mi kad je baza dostupna — tada ću na kraju pokrenuti pun `mvnw clean install`.

Do tada radim isključivo s `mvnw clean compile` koji ne treba bazu.

---

## FAZA 1 — Package rename

### Korak 1.1 — `hr.algebra.talaria` → `hr.algebra.ledvision`
**Status:** ✅ gotovo (commit `00f75d5`)
**Fajlovi:** svi `.java` fajlovi (package deklaracije + importi), `pom.xml` (`artifactId`, `name`, `description`), `application.properties` (`spring.application.name`), `TalariaApplication.java` → `LedVisionApplication.java`, `TalariaApplicationTests.java` → `LedVisionApplicationTests.java`, folder struktura `src/main/java/hr/algebra/talaria/` → `src/main/java/hr/algebra/ledvision/`.
**Opis:** Čisto mehanički rename, bez promjene logike. Radi se u jednom commitu jer je nedjeljiv (Java ne kompajlira polovično promijenjen package).
**Provjera:** `mvnw clean compile`

---

## FAZA 2 — Category → Location

### Korak 2.1 — Rename entiteta i svih slojeva
**Status:** ✅ gotovo
**Fajlovi:**
- `model/Category.java` → `model/Location.java` (`@Table(name="categories")` → `"locations"`)
- `repository/CategoryRepository.java` → `LocationRepository.java`
- `dto/CategoryDto.java` → `LocationDto.java`
- `mapper/CategoryMapper.java` → `LocationMapper.java`
- `service/CategoryService.java` → `LocationService.java`
- `controller/api/CategoryApiController.java` → `LocationApiController.java` (`/api/category` → `/api/locations`)
- Reference u `ProductService`/budući `AdSpacePackageService`, `AdminViewController`, `ProductViewController`/budući `AdSpacePackageViewController`
- Templates: `admin/category-form.html` → `admin/location-form.html`, reference u `admin/dashboard.html`, `products/list.html` (filter po lokaciji), `index.html`
- `SecurityConfig` — matcheri `/categories/**` (trenutno već postoje kao permitAll, samo provjeriti da i dalje odgovaraju)

**Provjera:** `mvnw clean compile`

---

## FAZA 3 — Product → AdSpacePackage (čisti rename, bez strukturnih izmjena)

### Korak 3.1 — Rename entiteta i svih slojeva (polja ostaju ista za sada)
**Status:** ✅ gotovo
**Fajlovi:**
- `model/Product.java` → `model/AdSpacePackage.java` (`@Table(name="products")` → `"ad_space_packages"`, `category` polje → `location`)
- `repository/ProductRepository.java` → `AdSpacePackageRepository.java`
- `dto/ProductDto.java` → `AdSpacePackageDto.java`
- `mapper/ProductMapper.java` → `AdSpacePackageMapper.java`
- `service/ProductService.java` → `AdSpacePackageService.java`
- `controller/api/ProductApiController.java` → `AdSpacePackageApiController.java` (`/api/products` → `/api/packages`)
- `controller/mvc/ProductViewController.java` → `AdSpacePackageViewController.java` (`/products` rute mogu ostati kao URL-i radi jednostavnosti, ili se mijenjaju u `/packages` — **predlažem zadržati `/products` kao URL rutu privremeno pa je promijeniti u istoj fazi zajedno s navigacijom**, javi ako želiš drugačije)
- Templates: `products/list.html`, `products/detail.html`, `admin/product-form.html` → preimenovani nazivi varijabli (`product`→`pkg`/`adPackage`), ali **cijene i stock quantity UI polja ostaju zasad netaknuta** (uklanjaju se u Fazi 4)

**Napomena:** `price` i `stockQuantity` polja **ostaju** na entitetu u ovom koraku — samo je entitet preimenovan. Uklanjaju se tek kad `PricingTier` preuzme tu ulogu (Faza 4), da app ostane funkcionalan cijelo vrijeme.

**Provjera:** `mvnw clean compile`

---

## FAZA 4 — PricingTier (nova cjenovna struktura)

### Korak 4.1 — Dodaj PricingTier entitet (aditivno, ništa se ne briše)
**Status:** ✅ gotovo
**Fajlovi (novi):** `model/PricingTier.java`, `repository/PricingTierRepository.java`, `dto/PricingTierDto.java`, `mapper/PricingTierMapper.java`, `service/PricingTierService.java`

**Polja PricingTier:**
```
id, adSpacePackage (ManyToOne), durationLabel (String, npr. "1 tjedan"/"1 mjesec"/"3 mjeseca"),
sizeLabel (String, npr. "Mali ekran (2m²)"/"Veliki ekran (6m²)"), price (BigDecimal)
```

**Provjera:** `mvnw clean compile`

### Korak 4.2 — Admin CRUD za tier-ove
**Status:** ✅ gotovo
**Fajlovi:** novi endpoint-i u `AdminViewController` (`/admin/packages/{packageId}/tiers/new`, `/save`, `/delete/{id}`), nova forma `admin/tier-form.html`, prikaz liste tier-ova na `admin/product-form.html` (edit paketa prikazuje i njegove tier-ove ispod)
**Provjera:** `mvnw clean compile`

### Korak 4.3 — CUTOVER: ukloni `price`/`stockQuantity` s AdSpacePackage; Cart i Order rade preko tier-a
**Status:** ⏳ čeka
**Status:** ✅ gotovo

**Ovo je najveći i najrizičniji korak** — mora ići u jednom commitu jer cijeli lanac (cart → checkout → order) mora ostati konzistentan.

**Fajlovi:**
- `model/AdSpacePackage.java` — ukloni `price`, `stockQuantity`
- `model/OrderItem.java` — `product` → `pricingTier` (ManyToOne na `PricingTier`)
- `service/CartService.java` — cart mapa `Map<Long productId, Integer qty>` → `Map<Long tierId, Integer qty>`; `getItemTotal`/`getCartTotal` čitaju `tier.getPrice()`
- `dto/CartAddRequest.java`, `dto/CartUpdateRequest.java` — `productId` → `tierId`
- `controller/api/CartApiController.java`, `controller/mvc/CartViewController.java` — ažurirane reference
- `static/js/cart.js` — `productId` parametar → `tierId`
- `service/OrderService.java` — `createOrder` čita cijenu iz `pricingTier.getPrice()` umjesto `product.getPrice()`, provjera dostupnosti mijenja se (nema više `stockQuantity`, provjerava se samo da tier postoji i da je paket aktivan)
- `mapper/OrderMapper.java`, `dto/OrderItemDto.java` — `productId/productName` → `pricingTierId` + `packageName` + `tierLabel`
- Templates: `cart/cart.html`, `orders/checkout.html`, `orders/detail.html`, `orders/history.html`, `admin/orders.html` — prikazuju naziv paketa + tier umjesto naziva proizvoda

**Provjera:** `mvnw clean compile`

---

## FAZA 5 — AdExample (galerija primjera reklama: slike I videi)

### Korak 5.1 — Dodaj AdExample entitet + CRUD (aditivno)
**Status:** ✅ gotovo
**Fajlovi (novi):** `model/AdExample.java` (`id, adSpacePackage (ManyToOne), mediaUrl, mediaType (enum IMAGE/VIDEO), caption`), `repository/AdExampleRepository.java`, `dto/AdExampleDto.java`, `mapper/AdExampleMapper.java`, servisne metode u `AdSpacePackageService` ili novi `AdExampleService`, admin CRUD forma/rute (slično tier-ovima), prikaz galerije na `products/detail.html` (budući `packages/detail.html`) — slike se prikazuju kao `<img>`, videi kao `<video controls>` ovisno o `mediaType`.
**Provjera:** `mvnw clean compile`

### Korak 5.2 — [MANUAL] Ubacivanje tvojih slika i videa
**Status:** ⏳ čeka

Kad ovaj korak stigne na red, stat ću i javiti točno ovo:
1. Kreiraj foldere (ako ne postoje): `src/main/resources/static/images/locations/`, `src/main/resources/static/images/packages/`, `src/main/resources/static/images/ads/` (slike primjera reklama), `src/main/resources/static/videos/ads/` (video primjeri reklama)
2. Spremi slike/videe unutra (npr. `images/ads/primjer1.jpg`, `videos/ads/primjer1.mp4`)
3. U admin panelu, kod uređivanja primjera reklame za paket, upiši putanju (`/images/ads/primjer1.jpg` ili `/videos/ads/primjer1.mp4`) i odaberi tip (Image/Video) — ili puni eksterni URL ako hostaš negdje vani, isto kao trenutni Talaria hero image s Cloudflare R2.

Nikakav dodatni kod nije potreban — `/images/**` je već javno servirano; za `/videos/**` ću dodati isti permitAll matcher u `SecurityConfig` u sklopu Koraka 5.1.

---

## FAZA 6 — UI / Branding pass

### Korak 6.1 — Tekstualne izmjene (kozmetika, nema strukturnih rizika)
**Status:** ⏳ čeka
**Fajlovi:** `fragments/navbar.html` (TALARIA → LEDVISION, "Products"→"Packages", "🛒 Cart" ostaje ili se mijenja u "Reservations"), `index.html` (hero tekst "RIDE THE FUTURE"/"Electric Mobility" → LED-oglašavanje tekst, "Shop by Category"→"Browse Locations", "Featured Products"→"Featured Packages"), `products/list.html`, `admin/dashboard.html` (tab nazivi), `fragments/head.html` (title fallback), ikone (⚡ placeholderi → npr. 💡/📺)
**[MANUAL napomena]:** trenutni hero image je eksterni URL specifičan za Talaria motocikl. Trebat ću od tebe URL nove hero slike (ili ću staviti generički placeholder/gradient dok ne dostaviš sliku) — javit ću kad dođemo do ovog koraka.
**Provjera:** `mvnw clean compile`

---

## FAZA 7 — Finalna provjera

### Korak 7.1 — [MANUAL CHECKPOINT] Baza spojena → pun build
**Status:** ⏳ čeka
Nakon što potvrdiš da su env varijable iz Koraka 0.1 postavljene i baza dostupna, pokrećem `mvnw clean install` (puni build s testovima) i zatim app lokalno (`mvnw spring-boot:run`), te prolazim kroz golden path u browseru: home → lokacije → detalji paketa → dodavanje tier-a u košaricu → checkout (cash) → admin CRUD (lokacije/paketi/tier-ovi/primjeri) → login history.

### Korak 7.2 — [MANUAL, kasnije] Deploy na hosting
**Status:** ⏳ čeka
Zadatak traži postavljanje na hosting/Tomcat prije prezentacije — ovo namjerno ostavljam za posebnu, kasniju fazu nakon što domena radi lokalno, jer je to zaseban infra zadatak (izbor hostinga, environment na serveru, itd.). Javi kad želiš da se ovim pozabavimo.

---

## Sažetak statusa

| Faza | Opis | Status |
|---|---|---|
| 0.1 | Env varijable (MANUAL) | ⏳ |
| 1.1 | Package rename | ⏳ |
| 2.1 | Category → Location | ⏳ |
| 3.1 | Product → AdSpacePackage (rename) | ⏳ |
| 4.1 | PricingTier entitet | ⏳ |
| 4.2 | Admin CRUD za tier-ove | ⏳ |
| 4.3 | Cutover: cart/order preko tier-a | ⏳ |
| 5.1 | AdExample entitet + CRUD | ⏳ |
| 5.2 | Ubacivanje slika (MANUAL) | ⏳ |
| 6.1 | UI/branding pass | ⏳ |
| 7.1 | Pun build + smoke test (MANUAL checkpoint) | ⏳ |
| 7.2 | Deploy (MANUAL, kasnije) | ⏳ |
