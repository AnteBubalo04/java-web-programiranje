# DEEP_DIVE_GUIDE.md — Potpuni, temeljit vodič kroz LedVision projekt

> Cilj ovog dokumenta: da nakon što ga pročitaš i prođeš vježbe, možeš **objasniti bilo koji dio ovog projekta bez pripreme**, i da razumiješ ne samo *što* kod radi nego *zašto* je napisan baš tako i *kako bi ga sam napisao od nule*.
>
> Piše se za apsolutnog početnika u Spring Bootu — ako nešto već znaš, preskoči, ali nemoj preskakati poglavlja jer se svako oslanja na prethodno.

---

# DIO 0 — Pojmovi koje moraš znati prije nego dotakneš kod

## 0.1 Što je uopće Spring Boot?

Zamisli da želiš napraviti web aplikaciju u Javi bez frameworka. Morao bi ručno:
- podići HTTP server
- ručno parsirati svaki HTTP request
- ručno upravljati konekcijama na bazu
- ručno pisati SQL za svaku operaciju
- ručno povezivati sve klase jednu s drugom (ručno kreirati instance i predavati ih kao argumente)

**Spring Framework** je skup biblioteka koje to sve rade umjesto tebe, kroz koncept zvan **Dependency Injection (DI)** — objašnjeno u 0.3.

**Spring Boot** je "razumna zadana konfiguracija" (sensible defaults) preko Spring Frameworka — umjesto da ti ručno konfiguriraš 50 stvari, Spring Boot pogodi razumne postavke na temelju toga koje si biblioteke (dependencies) stavio u projekt, i ti samo prepišeš ono što želiš drugačije.

## 0.2 Što je Maven i što je `pom.xml`?

Maven je **build alat** — on:
1. Skida sve biblioteke (dependencies) koje si naveo u `pom.xml` s interneta (Maven Central repozitorij) u tvoj lokalni `.m2` folder
2. Kompajlira tvoj Java kod
3. Pokreće testove
4. Pakira sve u jedan izvršni `.jar` fajl

`pom.xml` = "Project Object Model" — XML fajl gdje pišeš: koje biblioteke trebaš (`<dependency>`), koju verziju Jave koristiš, koje plugin-e za build.

Naredba `mvnw clean compile` koju smo koristili cijelo vrijeme: `clean` obriše stari build output, `compile` prevede sav Java kod u bytecode. `mvnw` (Maven Wrapper) je skripta koja osigurava da svatko tko otvori projekt koristi **istu** verziju Mavena, bez da je mora ručno instalirati.

## 0.3 Dependency Injection (DI) i Inversion of Control (IoC) — NAJVAŽNIJI koncept

Ovo je temelj svega. Bez DI, da tvoj `AdSpacePackageService` treba `AdSpacePackageRepository`, morao bi ručno pisati:

```java
// BEZ Springa - ručno
AdSpacePackageRepository repo = new AdSpacePackageRepository(...);
AdSpacePackageService service = new AdSpacePackageService(repo);
```

I to bi morao raditi za **svaku** klasu, ručno, u točnom redoslijedu (prvo repository, pa servis koji ga treba, pa kontroler koji treba servis...). Za veliku aplikaciju to postane noćna mora.

**Sa Springom**, ti samo kažeš "ova klasa treba tu drugu klasu" kroz konstruktor, a Spring **sam** kreira sve instance i "ubrizga" (injectuje) ih gdje trebaju:

```java
@Service
@RequiredArgsConstructor  // Lombok generira konstruktor iz svih "final" polja
public class AdSpacePackageService {
    private final AdSpacePackageRepository packageRepository;  // Spring OVO sam ubrizga
    private final LocationRepository locationRepository;        // i OVO
}
```

Spring pri pokretanju aplikacije skenira sve klase (`@ComponentScan`, iz `@SpringBootApplication`), vidi da `AdSpacePackageService` treba `AdSpacePackageRepository` i `LocationRepository` u konstruktoru, kreira te instance (ili ih već ima kreirane), i proslijedi ih automatski. Ti se nikad ne baviš sa `new AdSpacePackageService(...)` — to Spring radi.

Objekt koji Spring kreira i njime upravlja zove se **Bean**. "IoC kontejner" (Inversion of Control) je Spring-ov interni sustav koji drži sve te bean-ove i zna kako ih povezati.

**Kako Spring zna koje klase treba pretvoriti u bean-ove?** Preko anotacija: `@Service`, `@Repository`, `@Controller`, `@RestController`, `@Component`, `@Configuration` — sve su to "stereotype" anotacije koje kažu Springu "ovo je bean, upravljaj mnome".

## 0.4 Slojevita arhitektura (Layered Architecture)

Cijeli projekt je organiziran u slojeve, svaki sa svojom odgovornošću:

```
HTTP request
    ↓
CONTROLLER   (prima HTTP request, poziva servis, vraća response/view)
    ↓
SERVICE      (poslovna logika — "što se stvarno treba dogoditi")
    ↓
REPOSITORY   (razgovor s bazom podataka)
    ↓
DATABASE
```

Zašto ne staviti sve u jednu klasu? Jer:
- **Odvajanje odgovornosti** (Separation of Concerns) — svaki sloj radi jednu stvar, lakše je testirati i mijenjati
- Kontroler se ne treba brinuti KAKO se podatak sprema u bazu
- Servis se ne treba brinuti je li poziv došao s weba ili API-ja
- Repository se ne treba brinuti tko ga poziva

## 0.5 Što je JPA i Hibernate?

**JPA** (Jakarta Persistence API) je **specifikacija** (skup pravila/interfacea) za "kako Java objekti postaju redovi u bazi podataka i obrnuto" — to se zove **ORM** (Object-Relational Mapping).

**Hibernate** je **implementacija** te specifikacije — stvarna biblioteka koja to i radi. Kad pišeš `@Entity` na klasu, JPA/Hibernate zna da ta klasa odgovara tablici u bazi.

Bez ORM-a, morao bi ručno pisati:
```java
String sql = "SELECT * FROM ad_space_packages WHERE id = ?";
PreparedStatement stmt = connection.prepareStatement(sql);
stmt.setLong(1, id);
ResultSet rs = stmt.executeQuery();
AdSpacePackage p = new AdSpacePackage();
p.setId(rs.getLong("id"));
p.setName(rs.getString("name"));
// ... za svako polje ručno
```

S JPA/Hibernateom, samo pozoveš `packageRepository.findById(id)` i dobiješ gotov Java objekt.

**Spring Data JPA** je dodatni sloj preko JPA/Hibernatea koji ti daje `JpaRepository` interface s gotovim metodama (`save`, `findById`, `findAll`, `delete`) i mogućnost da metode kao `findByUsername(String username)` **automatski** generiraju SQL samo iz imena metode.

## 0.6 Što je REST i MVC?

**MVC** (Model-View-Controller) — arhitekturni pattern gdje:
- **Model** = podaci (tvoji entiteti/DTO-ovi)
- **View** = ono što korisnik vidi (Thymeleaf HTML stranice)
- **Controller** = "posrednik" — prima zahtjev, dohvati/mijenja Model, bira koji View vratiti

**REST** (Representational State Transfer) — stil dizajniranja API-ja gdje:
- Svaki resurs ima svoj URL (npr. `/api/packages/5`)
- Koristiš HTTP metode za akcije: `GET` (dohvati), `POST` (kreiraj), `PUT` (ažuriraj), `DELETE` (obriši)
- Server ne pamti stanje između zahtjeva (**stateless**) — svaki zahtjev nosi sve što treba (npr. JWT token)

U tvom projektu imaš **oba** — MVC kontrolere koji vraćaju Thymeleaf HTML stranice (`@Controller`), i REST kontrolere koji vraćaju JSON (`@RestController`).

## 0.7 Anotacije koje ćeš vidjeti posvuda — brzi rječnik

| Anotacija | Značenje |
|---|---|
| `@Entity` | Ova klasa = tablica u bazi |
| `@Table(name="...")` | Eksplicitno ime tablice (inače bi Hibernate koristio ime klase) |
| `@Id` | Ovo polje je primarni ključ |
| `@GeneratedValue` | Baza sama generira vrijednost (auto-increment) |
| `@Column` | Detalji o stupcu (nullable, length, unique...) |
| `@ManyToOne` / `@OneToMany` | Relacije između entiteta (foreign key) |
| `@Service` | Klasa u servisnom sloju, Spring bean |
| `@Repository` | Klasa/interface u repository sloju |
| `@Controller` | MVC kontroler (vraća view imena) |
| `@RestController` | REST kontroler (vraća JSON, `= @Controller + @ResponseBody`) |
| `@RequestMapping`, `@GetMapping`, `@PostMapping` | Mapira URL + HTTP metodu na Java metodu |
| `@Autowired` (rijetko koristiš, radiš preko konstruktora) | Reci Springu da ubrizga ovisnost |
| `@RequiredArgsConstructor` (Lombok) | Generira konstruktor za sva `final` polja — omogućuje DI kroz konstruktor bez ručnog pisanja |
| `@Data` (Lombok) | Generira gettere, settere, `equals`, `hashCode`, `toString` |
| `@Transactional` | Metoda se izvršava unutar jedne baznе transakcije (sve uspije ili se sve poništi) |
| `@PreAuthorize("...")` | Provjera dozvola PRIJE nego se metoda uopće pozove |
| `@Value("${...}")` | Ubrizgaj vrijednost iz `application.properties` |

---

# DIO 1 — Arhitektura ovog konkretnog projekta

Sad kad znaš osnove, evo kako izgleda tvoj projekt konkretno, sloj po sloj, top-down:

```
src/main/java/hr/algebra/ledvision/
├── LedVisionApplication.java     ← ulazna točka
├── config/                        ← konfiguracijske klase (OpenAPI, PayPal)
├── model/                         ← @Entity klase (tablice u bazi)
├── repository/                    ← JpaRepository interfacei
├── dto/                           ← objekti za prijenos podataka (JSON)
├── mapper/                        ← entitet ↔ DTO pretvarači
├── service/                       ← poslovna logika
├── security/                      ← Spring Security konfiguracija, JWT
├── filter/                        ← servlet filteri
├── listener/                      ← event listeneri
├── controller/
│   ├── mvc/                       ← @Controller (Thymeleaf stranice)
│   └── api/                       ← @RestController (JSON REST API)
├── exception/                     ← globalno rukovanje greškama
└── utils/                         ← pomoćne klase (npr. CookieHelper)
```

Svaki paket = jedan sloj/odgovornost. Ovo NIJE slučajno — to je standardni Spring Boot layout koji ćeš vidjeti u gotovo svakom ozbiljnom projektu.

---

# DIO 2 — Domenski model, DUBOKO

## 2.1 `model/User.java` — temelj svega

```java
@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    @Setter(lombok.AccessLevel.NONE)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.BUYER;

    @Setter(lombok.AccessLevel.NONE)
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public enum Role {
        BUYER, ADMIN
    }
}
```

**Objašnjenje red po red:**

- `@GeneratedValue(strategy = GenerationType.IDENTITY)` — baza (PostgreSQL) sama generira ID koristeći auto-increment stupac. Postoje i druge strategije (`SEQUENCE`, `TABLE`) ali `IDENTITY` je najjednostavnija i najčešća za PostgreSQL.
- `unique = true` na `username` i `email` — baza NEĆE dopustiti dva reda s istom vrijednosti (Hibernate ovo pretvara u `UNIQUE` SQL constraint pri kreiranju tablice).
- `passwordHash` — **nikad** se ne sprema plain-text lozinka. Kod registracije, lozinka se hashira (jednosmjerna kriptografska funkcija — `BCryptPasswordEncoder`, vidjet ćeš u `SecurityConfig`) prije spremanja. Čak ni administrator baze ne može "pročitati" pravu lozinku.
- `@Enumerated(EnumType.STRING)` — Java enum (`Role.BUYER`/`Role.ADMIN`) sprema se u bazu kao **tekst** ("BUYER"/"ADMIN"), ne kao broj (`EnumType.ORDINAL` bi spremao 0/1 — loše, jer ako kasnije promijeniš redoslijed enum vrijednosti, svi stari podaci postanu pogrešni).
- `@Setter(lombok.AccessLevel.NONE)` na `role` i `createdAt` — Lombokov `@Data` inače generira i getter i setter za SVAKO polje. Ovdje eksplicitno kažemo "nemoj generirati setter" jer ta polja ne smiju biti proizvoljno mijenjana izvana (role se mijenja samo ručno u bazi/admin akcijom, createdAt se postavlja samo jednom).
- `@PrePersist` — JPA "lifecycle callback". Metoda označena ovom anotacijom se **automatski** pozove tik prije nego Hibernate izvrši `INSERT` u bazu. Ovdje postavljamo `createdAt` na trenutno vrijeme — korisnik/kod koji kreira `User` objekt ne mora ni razmišljati o tom polju.
- `implements Serializable` — objašnjeno detaljno u Dijelu 6 (sigurnost) — nužno jer se `User` (kroz `CustomUserDetails`) sprema u HTTP sesiju.

**Kako bi ovo napisao od nule:** Kreiraš novu klasu, staviš `@Entity`, dodaš polja s `@Column` gdje trebaš specifična pravila, `@Id` + `@GeneratedValue` za primarni ključ. To je to — Hibernate će pri pokretanju aplikacije (`spring.jpa.hibernate.ddl-auto=update` u `application.properties`) **sam** kreirati/ažurirati SQL tablicu na temelju ove klase. Ne pišeš `CREATE TABLE` ručno.

## 2.2 `model/Location.java`

```java
@Entity
@Table(name = "locations")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Location {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    @Column()
    private String imageUrl;
}
```

Jednostavan entitet — nema relacija prema van (nema `@ManyToOne`/`@OneToMany` ovdje), ali je **cilj** relacije iz `AdSpacePackage` (vidi 2.3). `unique = true` na `name` — ne mogu postojati dvije lokacije s istim imenom.

## 2.3 `model/AdSpacePackage.java` — prva relacija

```java
@Entity
@Table(name = "ad_space_packages")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdSpacePackage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 1000)
    private String description;

    @Column()
    private String imageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Location location;

    @Column(nullable = false)
    private boolean active = true;
}
```

**`@ManyToOne(fetch = FetchType.LAZY)`** — ovo je **jako važno** razumjeti:
- `@ManyToOne` znači: **mnogo** `AdSpacePackage` zapisa može pripadati **jednoj** `Location`. U bazi se to realizira kao stupac `category_id` u tablici `ad_space_packages` koji je **foreign key** prema `locations.id`.
- `fetch = FetchType.LAZY` znači: kad Hibernate dohvati `AdSpacePackage` iz baze, **NEĆE** odmah dohvatiti i povezani `Location` objekt — umjesto pravog objekta, stavlja "proxy" (privremeni placeholder). Tek kad **stvarno** pozoveš `adSpacePackage.getLocation().getName()`, Hibernate u tom trenutku napravi **dodatni** SQL upit da dohvati `Location`.

  Suprotno je `FetchType.EAGER` — odmah dohvati sve povezano, u istom upitu (obično preko JOIN-a).

  Zašto LAZY? Performanse — ako imaš listu od 100 paketa i ne trebaš svugdje njihovu lokaciju, ne želiš 100 nepotrebnih dodatnih upita. Ali ako TREBAŠ lokaciju odmah (npr. na detail stranici), moraš eksplicitno reći Hibernateu da je dohvati **u istom upitu** — to radiš s `JOIN FETCH` u repository sloju (Dio 3).

- `@JoinColumn(name = "category_id", ...)` — eksplicitno ime foreign key stupca u bazi. (Ostao je naziv `category_id` iz vremena kad se entitet zvao `Category` — funkcionalno je svejedno, samo je ime stupca "povijesno" ime, ne utječe na rad aplikacije.)

## 2.4 `model/PricingTier.java` — druga relacija, nosi cijenu

```java
@Entity
@Table(name = "pricing_tiers")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PricingTier {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "package_id", nullable = false)
    private AdSpacePackage adSpacePackage;

    @Column(nullable = false, length = 50)
    private String durationLabel;

    @Column(nullable = false, length = 50)
    private String sizeLabel;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;
}
```

**Zašto `BigDecimal` a ne `double`/`float` za cijenu?** Ovo je **klasično ispitno pitanje**. `double`/`float` su binarni brojevi s pomičnim zarezom — ne mogu **točno** predstaviti sve decimalne brojeve (npr. `0.1 + 0.2` u double aritmetici ne daje točno `0.3`, nego `0.30000000000000004`). Za novac to je neprihvatljivo — greške se akumuliraju. `BigDecimal` radi s **točnom** decimalnom aritmetikom, pa se uvijek koristi za novac.

`precision = 10, scale = 2` — ukupno 10 znamenki, od čega 2 nakon decimalne točke (npr. maksimalno `99999999.99`).

**Zašto `PricingTier` postoji kao poseban entitet umjesto da `AdSpacePackage` ima polje `price`?** Zato što jedan paket (npr. "Glavni ulaz Arena Centra") može imati **više** cjenovnih opcija — tjedan/mjesec/3 mjeseca, mali/veliki ekran. To je **1:N relacija** (jedan paket, mnogo tier-ova), pa mora biti poseban entitet, ne obično polje.

## 2.5 `model/AdExample.java`

```java
@Entity
@Table(name = "ad_examples")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdExample {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "package_id", nullable = false)
    private AdSpacePackage adSpacePackage;

    @Column(nullable = false, length = 500)
    private String mediaUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MediaType mediaType;

    @Column(length = 200)
    private String caption;

    public enum MediaType {
        IMAGE, VIDEO
    }
}
```

Isti pattern kao `PricingTier` — `@ManyToOne` prema paketu, plus `enum` za tip medija spremljen kao STRING (isti razlog kao kod `User.Role`).

## 2.6 `model/Order.java` i `model/OrderItem.java` — najsloženija relacija

```java
// Order.java (skraćeno)
@Entity
@Table(name = "orders")
public class Order {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    private BigDecimal totalPrice;
    private PaymentMethod paymentMethod;
    private OrderStatus status = OrderStatus.PENDING;
    // ... podaci o dostavi (shippingFullName, shippingAddress, itd.)
}
```

**`@OneToMany(mappedBy = "order", ...)`** — ovo je **suprotna strana** relacije. `OrderItem` ima `@ManyToOne` prema `Order` (vlasnik relacije, on ima foreign key stupac), a `Order` ima `@OneToMany(mappedBy = "order")` koji kaže "ja sam na 'jedan' strani, a foreign key je definiran na polju `order` unutar `OrderItem` klase". `mappedBy` znači "ne pravi ti dodatni join-tablicu, foreign key već postoji s druge strane".

- `cascade = CascadeType.ALL` — kad spremiš/obrišeš `Order`, automatski se ista operacija "kaskadno" primijeni i na sve njegove `OrderItem` zapise. Ne moraš ručno spremati svaki `OrderItem` posebno.
- `orphanRemoval = true` — ako makneš `OrderItem` iz `items` liste (npr. `order.getItems().remove(item)`), Hibernate ga **automatski obriše** iz baze (jer je postao "siroče" bez roditelja).

```java
// OrderItem.java
@Entity
@Table(name = "order_items")
public class OrderItem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private PricingTier pricingTier;

    private Integer quantity;
    private BigDecimal priceAtPurchase;
}
```

**`priceAtPurchase`** — ovo je **ključni koncept za ispit**: zašto ne samo pročitati `pricingTier.getPrice()` kad god trebamo cijenu narudžbe? Zato što se cijena tier-a **može promijeniti u budućnosti** (admin je poveća/smanji), a stara narudžba mora **zauvijek** pokazivati cijenu kakva je bila **u trenutku kupnje**. Zato se cijena "snapshot-a" (kopira) u `OrderItem.priceAtPurchase` u trenutku kreiranja narudžbe (vidi `OrderService.createOrder()`, Dio 5).

Cijeli lanac relacija: `User` → `Order` → `OrderItem` → `PricingTier` → `AdSpacePackage` → `Location`. Kad trebaš npr. "koja je lokacija paketa koji je netko kupio", moraš proći kroz **sve** te korake.

---

# DIO 3 — Repository sloj, DUBOKO

## 3.1 Osnovni `JpaRepository`

```java
@Repository
public interface LocationRepository extends JpaRepository<Location, Long> {
}
```

Ovo je **interface**, ne klasa! Ne pišeš implementaciju — Spring Data JPA je **automatski generira** za tebe pri pokretanju aplikacije (koristi tzv. dynamic proxy). `JpaRepository<Location, Long>` znači "repository za entitet `Location`, čiji je primarni ključ tipa `Long`". Time automatski dobiješ:
- `save(entity)` — INSERT ili UPDATE (ovisno ima li entitet već ID)
- `findById(id)` — vraća `Optional<Location>`
- `findAll()` — vraća `List<Location>`
- `deleteById(id)`
- `count()`, `existsById(id)`, i još desetke drugih

## 3.2 Query metode generirane iz imena metode

```java
public interface LocationRepository extends JpaRepository<Location, Long> {
    boolean existsByName(String name);
}
```

Spring Data JPA **parsira ime metode** i sam sastavi SQL. `existsByName` → "provjeri postoji li zapis gdje je `name` polje jednako danom parametru". Iza scene se generira otprilike:
```sql
SELECT COUNT(*) > 0 FROM locations WHERE name = ?
```

Drugi primjeri iz tvog koda: `findByUserIdAndCreatedAtBetween(Long userId, LocalDateTime from, LocalDateTime to)` — Spring parsira "By User Id And CreatedAt Between" i zna da treba `WHERE user_id = ? AND created_at BETWEEN ? AND ?`.

**Pravilo:** dokle god ime metode prati Spring Data JPA konvenciju (`findBy`/`existsBy`/`countBy`/`deleteBy` + imena polja + `And`/`Or`/`Between`/`GreaterThan`/... ), Spring će je razumjeti bez da ti napišeš ijedan SQL red.

## 3.3 Ručni upiti s `@Query` i JPQL

```java
@Query("SELECT p FROM AdSpacePackage p JOIN FETCH p.location WHERE p.id = :id")
Optional<AdSpacePackage> findByIdWithLocation(@Param("id") Long id);
```

Kad ime metode ne bi bilo dovoljno jasno ili trebaš specifičnu logiku (npr. `JOIN FETCH`), pišeš **JPQL** (Java Persistence Query Language) ručno unutar `@Query`.

**JPQL nije SQL!** Razlike:
- `FROM AdSpacePackage p` — koristiš ime **Java klase** (`AdSpacePackage`), ne ime tablice (`ad_space_packages`)
- `p.location` — koristiš ime **Java polja** (`location`), ne ime stupca (`category_id`)
- Hibernate **prevodi** JPQL u pravi SQL prilagođen tvojoj bazi (PostgreSQL, MySQL, itd. — JPQL je prenosiv između baza)

`JOIN FETCH p.location` — eksplicitno kažeš "u ISTOM upitu odmah dovuci i `location`", zaobilazeći LAZY fetch ponašanje spomenuto u 2.3. Ovo je razlog zašto ovaj upit postoji — bez njega bi `location` bio "lijen" i mogao baciti grešku kad ga template pokuša ispisati nakon što se JPA sesija zatvori (`LazyInitializationException` — vrlo česta greška početnika, upamti ovo za ispit!).

`@Param("id")` — povezuje Java parametar metode s `:id` placeholderom u JPQL stringu.

## 3.4 Composite upit s više JOIN FETCH-ova — `OrderRepository`

```java
@Query("SELECT DISTINCT o FROM Order o " +
        "JOIN FETCH o.user " +
        "LEFT JOIN FETCH o.items i "+
        "LEFT JOIN FETCH i.pricingTier t "+
        "LEFT JOIN FETCH t.adSpacePackage p "+
        "LEFT JOIN FETCH p.location WHERE o.id = :id")
Optional<Order> findByIdWithItems(@Param("id") Long id);
```

Ovaj upit dohvaća **cijeli lanac** relacija u jednom SQL upitu (Hibernate ga prevede u JOIN-ove): narudžba → korisnik → stavke → tier-ovi → paketi → lokacije. Zašto? Da stranica s detaljima narudžbe (`orders/detail.html`) može ispisati SVE te podatke (ime paketa, lokaciju, tier) bez da baci grešku ili napravi desetke dodatnih upita (tzv. **N+1 problem** — kad bi za svaku od N stavki napravio poseban upit za njen tier, pa poseban za paket, itd.).

`LEFT JOIN FETCH` umjesto `JOIN FETCH` — `LEFT JOIN` vraća red i ako povezani entitet ne postoji (npr. narudžba bez ijedne stavke); obični `JOIN` bi tu narudžbu **izbacio** iz rezultata.

`DISTINCT` — kad JOIN-aš `Order` s njegovom listom `items`, ako narudžba ima 3 stavke, dobio bi **3 reda** za istu narudžbu (SQL JOIN prirodno duplicira roditeljski red za svaki child red). `DISTINCT` na Java/Hibernate razini makne te duplikate iz konačne Java liste.

---

# DIO 4 — DTO i Mapper sloj, DUBOKO

## 4.1 Zašto uopće DTO kad već imamo entitet?

**Nikad ne vraćaj JPA entitete direktno kao JSON iz REST kontrolera.** Razlozi:

1. **Beskonačna petlja / preveliki odgovor** — `AdSpacePackage` ima `location`, koji možda (u nekoj budućoj verziji) ima listu paketa natrag, koji imaju svoju lokaciju... Jackson (biblioteka za JSON serijalizaciju) bi mogao pući ili vratiti ogroman, duboko ugniježđen JSON.
2. **Sigurnost** — entitet možda ima polja koja **ne smiješ** izložiti klijentu (npr. `User.passwordHash` — da ga vratiš u JSON-u, svatko tko pozove `/api/users/5` bi vidio hash lozinke).
3. **Lazy loading problemi** — ako Jackson pokuša serijalizirati LAZY polje **nakon** što se JPA sesija zatvorila (tipično nakon što je kontroler završio), baca grešku.
4. **Kontrola oblika API-ja** — DTO ti daje potpunu kontrolu nad tim koji točno JSON oblik klijent vidi, neovisno kako izgleda baza iznutra. Možeš mijenjati bazu bez da "slomiš" API ugovor.

## 4.2 Primjer

```java
// Entitet - ima JPA anotacije, relacije, lazy loading
@Entity
public class AdSpacePackage {
    private Long id;
    private String name;
    private Location location;  // cijeli objekt, LAZY
    // ...
}

// DTO - obični POJO (Plain Old Java Object), samo podaci
@Data @NoArgsConstructor @AllArgsConstructor
public class AdSpacePackageDto {
    private Long id;
    private String name;
    private String imageUrl;
    private Long locationId;      // samo ID, ne cijeli objekt
    private String locationName;  // samo ime, "spljošteno"
}
```

## 4.3 Mapper — ručno pisana pretvorba

```java
public class AdSpacePackageMapper {
    private AdSpacePackageMapper() {}  // privatan konstruktor - ovo je "utility klasa", nikad se ne instancira

    public static AdSpacePackageDto toDto(AdSpacePackage p) {
        return new AdSpacePackageDto(
                p.getId(), p.getName(), p.getDescription(), p.getImageUrl(),
                p.getLocation() != null ? p.getLocation().getId() : null,
                p.getLocation() != null ? p.getLocation().getName() : null
        );
    }

    public static AdSpacePackage toEntity(AdSpacePackageDto dto) {
        AdSpacePackage p = new AdSpacePackage();
        p.setName(dto.getName());
        // ...
        return p;
    }
}
```

`private AdSpacePackageMapper() {}` — spriječi da netko slučajno napravi `new AdSpacePackageMapper()`. Klasa postoji samo za svoje `static` metode.

`p.getLocation() != null ? ... : null` — **null-safety provjera**. Zašto bi `location` ikad bio `null` kad je polje `nullable = false` u bazi? Jer ovo je **obrambeni kod** (defensive programming) — Java kod ne zna garantirano da je baza uvijek konzistentna (npr. LAZY proxy koji nije inicijaliziran može se ponašati čudno prije prvog pristupa), pa je sigurnije provjeriti.

**U velikim projektima** ovo se često automatizira bibliotekama poput **MapStruct** (generira mapper kod pri kompajliranju) umjesto ručnog pisanja — ali ručno pisanje kao ovdje je potpuno legitiman i lakše razumljiv pristup za manji projekt, i **bolje je za ispit** jer točno vidiš i kontroliraš svaku liniju.

---

# DIO 5 — Servisni sloj, DUBOKO

## 5.1 Zašto servisni sloj, kad kontroler može direktno zvati repository?

Tehnički **može**, ali:
1. Poslovna logika (npr. "provjeri je li korisnik uopće vlasnik ove narudžbe prije nego mu je pokažeš") ne pripada kontroleru — kontroler bi trebao biti "tanak", samo primiti HTTP i delegirati.
2. Ista logika se često treba iz **više** kontrolera (MVC i REST) — bez servisa, morao bi duplicirati kod.
3. `@Transactional` (vidi 5.3) se stavlja na servisnu metodu, ne na kontroler.

## 5.2 `CartService` — pažljivo pročitaj

```java
@Service
@RequiredArgsConstructor
public class CartService {
    private final PricingTierRepository tierRepository;

    public void addToCart(Map<Long, Integer> cart, Long tierId, int quantity) {
        tierRepository.findById(tierId).ifPresent(tier -> {
            if (quantity > 0) {
                cart.merge(tierId, quantity, Integer::sum);
            }
        });
    }
    // ...
}
```

- `Map<Long, Integer> cart` — cart NIJE spremljen u bazi! Prima se kao **parametar** izvana (iz `HttpSession`, vidi kontrolere). `CartService` je "glup" u tom smislu — on ne zna gdje cart živi, samo dobije mapu i vrati izmijenjenu/pročita iz nje. Ovo je **namjeran dizajn** — cart treba raditi i za anonimne korisnike (nema `User` na kojeg bi ga vezao u bazi dok se ne prijavi).
- `tierRepository.findById(tierId).ifPresent(tier -> {...})` — `findById` vraća `Optional<PricingTier>` (može postojati ili ne). `.ifPresent(lambda)` znači "ako postoji, izvrši ovaj kod s njim; ako ne postoji, tiho ne radi ništa" — elegantan način izbjegavanja `if (x != null)` provjera i `NullPointerException`-a.
- `cart.merge(tierId, quantity, Integer::sum)` — `Map.merge()` je Java standardna metoda: ako `tierId` već postoji u mapi, **zbroji** staru i novu količinu (`Integer::sum` je "method reference", kraći zapis za `(a, b) -> a + b`); ako ne postoji, samo doda novi par.

## 5.3 `OrderService.createOrder()` — srce cijele kupovine

```java
@Transactional
public Order createOrder(Long userId, Map<Long, Integer> cart, ...) {
    User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found!"));

    Order order = new Order();
    order.setUser(user);
    // ... postavljanje polja narudžbe

    BigDecimal total = BigDecimal.ZERO;

    for (Map.Entry<Long, Integer> entry : cart.entrySet()) {
        PricingTier tier = tierRepository.findById(entry.getKey())
                .orElseThrow(() -> new IllegalArgumentException("Pricing tier not found!"));

        OrderItem item = new OrderItem();
        item.setOrder(order);
        item.setPricingTier(tier);
        item.setQuantity(entry.getValue());
        item.setPriceAtPurchase(tier.getPrice());  // SNAPSHOT cijene!

        order.getItems().add(item);
        total = total.add(tier.getPrice().multiply(BigDecimal.valueOf(entry.getValue())));
    }

    order.setTotalPrice(total);
    return orderRepository.save(order);
}
```

**`@Transactional`** — ovo je **jedno od najvažnijih ispitnih pitanja**. Znači: sve baze operacije unutar ove metode se izvršavaju kao **jedna atomska transakcija**. Ako bilo koji dio metode baci exception (npr. `orElseThrow` jer tier ne postoji), **SVE** promjene se poništavaju (rollback) — nijedan djelomičan `Order` neće ostati u bazi. Bez `@Transactional`, ako bi pukla greška nakon što si spremio 2 od 3 `OrderItem`, imao bi "napola" kreiranu narudžbu u bazi — loše stanje.

`orElseThrow(() -> new IllegalArgumentException(...))` — ako `Optional` ne sadrži vrijednost, baci danu iznimku. Ovo je čitljiviji ekvivalent od:
```java
Optional<User> opt = userRepository.findById(userId);
if (opt.isEmpty()) throw new IllegalArgumentException("User not found!");
User user = opt.get();
```

`order.getItems().add(item)` — primijeti da NE pozivaš `orderItemRepository.save(item)` ručno! Sjeti se `cascade = CascadeType.ALL` na `Order.items` polju (Dio 2.6) — kad na kraju pozoveš `orderRepository.save(order)`, Hibernate **automatski** kaskadno spremi i sve `OrderItem` objekte iz liste. Ovo je direktna primjena onoga što smo naučili o cascade-u.

`total.add(tier.getPrice().multiply(BigDecimal.valueOf(entry.getValue())))` — primijeti da se `BigDecimal` **ne** koristi s običnim `+`/`*` operatorima (Java to ne dopušta za objekte) — koristiš metode `.add()`, `.multiply()`, itd. `BigDecimal` je **immutable** (nepromjenjiv) — svaka operacija vraća **novi** `BigDecimal`, ne mijenja postojeći.

---

# DIO 6 — Sigurnost i JWT, OD NULE

## 6.1 Što uopće JWT je?

**JWT** (JSON Web Token) je string sastavljen od tri dijela odvojena točkama, npr:
```
eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhbnRlIiwiZXhwIjoxNzA5...  .SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c
```

1. **Header** (base64-kodiran JSON) — koji algoritam je korišten za potpis
2. **Payload/Claims** (base64-kodiran JSON) — podaci: username, datum isteka (`exp`), itd. — **NIJE ŠIFRIRANO**, samo kodirano! Svatko tko presretne token može pročitati sadržaj (ali ga ne može **promijeniti** neopaženo, zbog potpisa).
3. **Signature** — kriptografski potpis prva dva dijela, potpisan **tajnim ključem** (`jwt.secret` iz `application.properties`) koji zna samo server. Ako netko promijeni payload, potpis više neće odgovarati i server će odbiti token.

**Zašto JWT umjesto klasične sesije (cookie + server pamti stanje)?** JWT je **stateless** — server ne mora ništa pamtiti o tebi između zahtjeva, sve što treba je unutar tokena. Ovo je ključno za REST API-je koje koriste različiti klijenti (mobilna app, web, treći sustavi) — nema dijeljene sesije.

## 6.2 Access token vs Refresh token — zašto oba?

- **Access token** — kratkog vijeka (u tvom projektu 15 minuta, `jwt.expiration.access=900000` ms). Šalje se sa **svakim** API zahtjevom u `Authorization: Bearer <token>` headeru.
- **Refresh token** — dugog vijeka (7 dana, `jwt.expiration.refresh=604800000` ms). Koristi se **samo** da dobiješ **novi** access token kad stari istekne, bez da se korisnik ponovno mora prijaviti lozinkom.

**Zašto ne samo jedan token dugog vijeka?** Sigurnost — ako netko ukrade access token (npr. presretanjem, XSS napadom), on vrijedi samo 15 minuta pa se šteta ograniči. Refresh token se čuva sigurnije (u tvom projektu, kao **HttpOnly cookie** — JavaScript ga ne može pročitati, pa je otporniji na XSS) i rijetko se šalje.

## 6.3 `model/RefreshToken.java` — zašto refresh token živi u bazi

Za razliku od access tokena (koji server nikad ne sprema, samo ga generira i validira potpis), refresh token se **sprema u bazu** — zašto? Da bi ga administrator/sustav mogao **opozvati** (revoke) prije isteka — npr. kod odjave ili sumnje na krađu. Da je i refresh token samo "stateless" JWT bez zapisa u bazi, ne bi ga mogao poništiti prije prirodnog isteka.

## 6.4 `SecurityConfig.java` — dva filter chain-a

```java
@Bean
@Order(1)
public SecurityFilterChain apiFilterChain(HttpSecurity http) throws Exception {
    http.securityMatcher("/api/**")
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/packages/**").permitAll()
                .anyRequest().authenticated()
        )
        .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
    return http.build();
}

@Bean
@Order(2)
public SecurityFilterChain webFilterChain(HttpSecurity http) throws Exception {
    http.authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/packages/**", ...).permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
        )
        .formLogin(form -> form.loginPage("/auth/login")...)
        .logout(...);
    return http.build();
}
```

**Zašto DVA filter chaina?** Jer imaš **dva potpuno različita** načina autentikacije koja moraju raditi paralelno:
- `/api/**` zahtjevi — bez sesije (`STATELESS`), autentikacija kroz JWT `Authorization` header
- Sve ostalo (Thymeleaf stranice) — klasična sesijska autentikacija s login formom (`formLogin`)

`@Order(1)` / `@Order(2)` — Spring **redom** provjerava chain-ove; prvi čiji `securityMatcher` odgovara URL-u se koristi. `apiFilterChain` ima eksplicitan `securityMatcher("/api/**")` pa se primjenjuje SAMO na te URL-ove; `webFilterChain` (bez `securityMatcher`, znači "sve") hvata sve ostalo.

`sessionCreationPolicy(SessionCreationPolicy.STATELESS)` — eksplicitno kažemo Springu "ne kreiraj HTTP sesiju za ove zahtjeve uopće" — u skladu s "REST treba biti stateless" principom iz 6.1.

`.requestMatchers(HttpMethod.GET, "/api/packages/**").permitAll()` — primijeti **HttpMethod** filter! GET (čitanje) je javno, ali POST/PUT/DELETE na iste URL-ove **nisu** eksplicitno dopušteni ovdje, pa padaju pod `.anyRequest().authenticated()` — moraju imati valjan JWT.

`.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)` — ovdje se `JwtAuthFilter` **eksplicitno** ubacuje u Spring Security-in lanac filtera, **prije** standardnog filtera za login formom. Ovo govori Springu "prvo provjeri JWT, pa tek onda idi na daljnju logiku".

`.requestMatchers("/admin/**").hasRole("ADMIN")` — samo korisnici s `ROLE_ADMIN` autoritetom (vidi `CustomUserDetails.getAuthorities()`, gdje se `"ROLE_" + user.getRole().name()` gradi) smiju pristupiti `/admin/**` putanjama. Spring Security po konvenciji **uvijek** očekuje prefiks `"ROLE_"` u imenu autoriteta kad koristiš `hasRole("ADMIN")` (metoda sama doda taj prefiks kad uspoređuje).

## 6.5 `JwtAuthFilter.java` — kako se token stvarno provjerava

```java
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final UserDetailsServiceImpl userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);  // nema tokena, samo nastavi dalje
            return;
        }

        final String token = authHeader.substring(7);  // makni "Bearer " prefiks (7 znakova)
        if (!jwtService.isTokenValid(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        final String username = jwtService.extractUsername(token);
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authToken);
        }

        filterChain.doFilter(request, response);
    }
}
```

**`extends OncePerRequestFilter`** — Spring-ova bazna klasa koja garantira da se filter izvrši **točno jednom** po zahtjevu (bez ovoga, u nekim slučajevima servlet container može pozvati filter više puta za isti request — npr. kod internih forward-a).

**`FilterChain`** — ovo je Java Servlet API koncept (ne Spring specifično). HTTP zahtjev prolazi kroz **lanac** filtera prije nego stigne do stvarnog kontrolera — svaki filter može: (a) nešto napraviti prije/poslije, (b) odlučiti **zaustaviti** lanac (ne pozvati `filterChain.doFilter()`), ili (c) pustiti zahtjev dalje pozivom `filterChain.doFilter(request, response)`.

Logika ovdje: ako nema `Authorization` headera ili nije `Bearer` tipa → pusti zahtjev dalje bez postavljanja autentikacije (za javne endpointove ovo je OK, `SecurityConfig` će ionako odbiti pristup zaštićenima kasnije). Ako **ima** valjan token → dohvati `UserDetails` za tog korisnika i **ručno** postavi `Authentication` objekt u `SecurityContextHolder` — to je "signal" ostatku Spring Security-ja da je ovaj request autentificiran.

`SecurityContextHolder` — Spring-ov mehanizam koji drži trenutnu autentikaciju za **trenutnu nit izvršavanja** (thread-local varijabla). Svaki HTTP zahtjev se obrađuje na svojoj niti, pa svaki request ima svoj neovisan `SecurityContext`.

**VAŽNO** (sjeti se NPE bug-a koji smo popravili) — `@Component` na ovoj klasi znači da je Spring Boot **automatski** registrira kao **globalni servlet filter** na SVE zahtjeve (`/*`), ne samo `/api/**`, ČAK I kad je isti filter eksplicitno dodan samo u `apiFilterChain` preko `.addFilterBefore()`. Ovo je suptilna Spring Boot specifičnost — `@Component` na klasi koja implementira `Filter` (ili nasljeđuje `OncePerRequestFilter`, koji implementira `Filter`) automatski trigerira `FilterRegistrationBean` auto-konfiguraciju.

## 6.6 `CustomUserDetails.java` i `UserDetailsServiceImpl.java` — adapter pattern

Spring Security **ne zna ništa** o tvom `User` entitetu — on radi sa svojim vlastitim `UserDetails` interfaceom (definira `getUsername()`, `getPassword()`, `getAuthorities()`, itd.). `CustomUserDetails` je **adapter** (Adapter dizajnerski pattern) — omata tvoj `User` objekt i "prevodi" ga u oblik koji Spring Security razumije:

```java
public class CustomUserDetails implements UserDetails {
    private final User user;  // NE transient! (vidi zašto u Dijelu 6.7)

    public CustomUserDetails(User user) { this.user = user; }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
    }

    @Override
    public String getPassword() { return user.getPasswordHash(); }

    @Override
    public String getUsername() { return user.getUsername(); }
}
```

```java
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        return new CustomUserDetails(user);
    }
}
```

`UserDetailsService` je **standardni Spring Security interface** s jednom metodom — implementiraš ga da kažeš Springu "evo kako ti dohvatiti korisnika po username-u iz MOJE baze". Spring interno poziva ovu metodu kod svake provjere prijave.

## 6.7 Session serialization bug koji smo popravili — zašto je bio bug

Ovo je odličan primjer suptilnog, ali stvarnog bug-a — dobro za pokazati dubinsko razumijevanje na ispitu.

`UserDetails extends Serializable` (dio standardnog Spring Security ugovora). Kad se korisnik prijavi kroz `webFilterChain` (sesijska prijava), `Authentication` objekt (koji **sadrži** `CustomUserDetails` kao principal) se sprema u `HttpSession`. Spring Boot DevTools kod automatskog restarta (zbog promjene koda tijekom razvoja) triggera Tomcat da **serijalizira** aktivne sesije na disk prije gašenja, i **deserijalizira** ih natrag nakon ponovnog pokretanja — da korisnik ne izgubi svoju sesiju/login zbog restart-a.

Polje `user` je **prije** bilo označeno `transient` — Java-in serialization mehanizam **eksplicitno preskače** `transient` polja (ne uključuje ih u serijalizirani zapis). Rezultat: nakon deserijalizacije, `CustomUserDetails` objekt je vraćen, ali `user` polje unutar njega je `null` — jer nikad nije ni bilo serijalizirano. Bilo koji kasniji poziv `getUsername()` (koji interno zove `user.getUsername()`) baca `NullPointerException`.

Popravak je dvodijelan:
1. Makni `transient` s polja `user` — dopusti da se stvarno serijalizira.
2. Dodaj `implements Serializable` na `User` entitet — jer da bi se `user` polje **uopće moglo** serijalizirati, i **on sam** mora biti `Serializable` (Java pravilo: svako polje unutar serijaliziranog objekta mora i samo biti serijalizirano, ili eksplicitno `transient`).

**Ispitno pitanje koje bi mogao dobiti:** "Što je `transient` i kad ga koristiš?" — odgovor: `transient` označava polje koje se **namjerno** isključuje iz Java serijalizacije (npr. lozinke, cache-irani izračunati podaci, connection handles koji se ne mogu/ne smiju prenijeti) — ali mora se koristiti **oprezno**, jer ako se objekt kasnije **stvarno** treba serijalizirati u cijelosti (kao ovdje, kroz HTTP sesiju), `transient` polje će nakon deserijalizacije biti `null`/default vrijednost.

---

# DIO 7 — Filteri i Listeneri, DUBOKO

## 7.1 Filter vs Listener — razlika

- **Filter** (Servlet API) — presreće **svaki HTTP zahtjev/odgovor** prije/poslije nego stigne do kontrolera. Aktivno sudjeluje u obradi zahtjeva, može ga zaustaviti ili modificirati.
- **Listener** — reagira na **evente** (događaje) koje Spring/Servlet container emitira, npr. "korisnik se uspješno prijavio", "sesija je kreirana/uništena". Listener **ne sudjeluje** direktno u obradi trenutnog HTTP zahtjeva — reagira **nakon** što se nešto već dogodilo, obično asinkrono/sporedno.

## 7.2 `listener/AuthenticationSuccessListener.java`

```java
@Component
@RequiredArgsConstructor
public class AuthenticationSuccessListener
        implements ApplicationListener<AuthenticationSuccessEvent> {

    private final AsyncService asyncService;

    @Override
    public void onApplicationEvent(AuthenticationSuccessEvent event) {
        // izvuci username, IP, user-agent iz eventa
        asyncService.recordLogin(username, ipAddress, userAgent);
    }
}
```

`ApplicationListener<AuthenticationSuccessEvent>` — Spring Security **automatski** emitira `AuthenticationSuccessEvent` nakon **svake** uspješne prijave (bilo kroz JWT ili sesijsku formu — oba mehanizma na kraju prolaze kroz Spring Security-in `AuthenticationManager`, koji emitira ovaj event). Tvoj listener se **automatski** pozove — ne moraš nigdje ručno pisati "pozovi ovaj listener nakon logina", Spring-ov event sustav (Observer pattern) to radi za tebe.

Ovo je **I6 zahtjev** iz specifikacije — "primijeni slušače (listeners)".

## 7.3 `service/AsyncService.java` i `@Async`

```java
@Service
public class AsyncService {
    @Async
    public void recordLogin(String username, String ip, String userAgent) {
        // spremi u LoginHistory tablicu
    }
}
```

`@Async` — metoda se izvršava na **posebnoj niti** (thread), **ne** na niti koja obrađuje trenutni HTTP zahtjev. Pozivatelj (`AuthenticationSuccessListener`) **ne čeka** da ova metoda završi — nastavlja odmah dalje. Ovo je **asinkrona funkcionalnost** koju specifikacija traži.

Da bi `@Async` uopće radio, mora postojati `@EnableAsync` anotacija negdje u konfiguraciji (provjeri gdje je u tvom projektu — vjerojatno na `LedVisionApplication` ili posebnoj `@Configuration` klasi). Bez `@EnableAsync`, `@Async` anotacija bi bila **tiho ignorirana** i metoda bi se izvršila sinkrono kao i svaka druga.

**Zašto asinkrono baš za login history?** Pisanje u bazu (audit log) nije nešto što korisnik treba čekati — njemu je bitno da se brzo prijavi i dobije odgovor. Spremanje log zapisa može se dogoditi "u pozadini" bez utjecaja na brzinu odgovora korisniku.

---

# DIO 8 — MVC kontroleri, DUBOKO

## 8.1 Anatomija jedne MVC metode

```java
@Controller
@RequiredArgsConstructor
public class AdSpacePackageViewController {
    private final AdSpacePackageService packageService;
    private final PricingTierService tierService;
    private final AdExampleService exampleService;

    @GetMapping("/packages/{id}")
    public String packageDetail(@PathVariable Long id, Model model) {
        packageService.getPackageById(id)
                .ifPresent(adSpacePackage -> model.addAttribute("pkg", adSpacePackage));
        model.addAttribute("tiers", tierService.getTiersByPackageId(id));
        model.addAttribute("examples", exampleService.getExamplesByPackageId(id));
        return "packages/detail";
    }
}
```

- `@Controller` (ne `@RestController`!) — vraćena vrijednost metode (`"packages/detail"`) se **ne** vraća direktno kao tekst odgovora, nego se tretira kao **ime view-a** (template fajla). Spring Boot + Thymeleaf auto-konfiguracija zna da treba pogledati u `src/main/resources/templates/packages/detail.html`.
- `@GetMapping("/packages/{id}")` — `{id}` je **path varijabla** (dio URL putanje, npr. `/packages/5` → `id = 5`).
- `@PathVariable Long id` — Spring **automatski** izvuče `{id}` iz URL-a i konvertira ga u `Long` (ako netko pošalje `/packages/abc`, Spring baci grešku prije nego tvoj kod uopće krene, jer "abc" nije broj).
- `Model model` — Spring **automatski** ubrizga prazan `Model` objekt za tebe. `model.addAttribute("pkg", ...)` stavlja podatak pod imenom "pkg" koji Thymeleaf template kasnije čita kao `${pkg}`.
- Tri poziva servisa — kontroler "sastavlja" podatke iz tri neovisna izvora za jednu stranicu. Ovo je normalno — kontroler koordinira, servisi rade posao.

## 8.2 Cijeli lanac jednog HTTP zahtjeva, korak po korak

Kad browser pošalje `GET /packages/5`:

1. Zahtjev prvo prolazi kroz **servlet filter lanac** (uključujući `JwtAuthFilter` — ali ovaj URL nema Authorization header pa se ništa posebno ne dogodi)
2. Spring Security provjerava `webFilterChain` pravila — `/packages/**` je `permitAll()`, prolazi
3. `DispatcherServlet` (Spring MVC-ov "glavni razvodnik") pronalazi koja metoda odgovara `GET /packages/5` — pronalazi `packageDetail(id=5, ...)`
4. Spring poziva tu metodu, ubrizgavajući `id=5` i prazan `Model`
5. Metoda poziva servise, puni `Model`, vraća string `"packages/detail"`
6. `DispatcherServlet` prosljeđuje kontrolu **ViewResolveru** koji taj string pretvori u pravi Thymeleaf template
7. Thymeleaf renderira `packages/detail.html`, koristeći podatke iz `Model`-a (`${pkg}`, `${tiers}`, `${examples}`)
8. Konačni HTML se šalje natrag browseru

---

# DIO 9 — REST API kontroleri, DUBOKO

## 9.1 Razlika od MVC kontrolera

```java
@RestController
@RequestMapping("/api/packages")
@RequiredArgsConstructor
public class AdSpacePackageApiController {
    private final AdSpacePackageService packageService;

    @GetMapping
    public List<AdSpacePackageDto> getAllPackages() {
        return packageService.getAllActivePackages().stream()
                .map(AdSpacePackageMapper::toDto)
                .toList();
    }
}
```

`@RestController` = `@Controller` + `@ResponseBody`. `@ResponseBody` znači: **ne** tretiraj povratnu vrijednost kao ime view-a — umjesto toga, **serijaliziraj je direktno** u HTTP response body (obično kao JSON, preko Jackson biblioteke koja je automatski uključena sa `spring-boot-starter-web`).

`.stream().map(AdSpacePackageMapper::toDto).toList()` — **Java Stream API**. `getAllActivePackages()` vraća `List<AdSpacePackage>` (entiteti). `.stream()` pretvara listu u stream (sekvencu elemenata za funkcionalnu obradu). `.map(AdSpacePackageMapper::toDto)` primijeni `toDto` funkciju na **svaki** element (pretvara `AdSpacePackage` → `AdSpacePackageDto`). `.toList()` skupi rezultat natrag u `List`. Ovo je kraći, deklarativni ekvivalent:
```java
List<AdSpacePackageDto> result = new ArrayList<>();
for (AdSpacePackage p : packageService.getAllActivePackages()) {
    result.add(AdSpacePackageMapper.toDto(p));
}
return result;
```

`AdSpacePackageMapper::toDto` — **method reference**, kraći zapis za lambdu `p -> AdSpacePackageMapper.toDto(p)`.

## 9.2 `ResponseEntity` — kontrola HTTP statusa

```java
@GetMapping("/{id}")
public ResponseEntity<AdSpacePackageDto> getPackage(@PathVariable Long id) {
    return packageService.getPackageById(id)
            .map(p -> ResponseEntity.ok(toDto(p)))
            .orElse(ResponseEntity.notFound().build());
}
```

`ResponseEntity<T>` ti daje kontrolu nad **cijelim** HTTP odgovorom — status kod, headeri, tijelo. `ResponseEntity.ok(dto)` = status 200 + dto kao JSON body. `ResponseEntity.notFound().build()` = status 404, prazno tijelo. Ovo je bolje od "samo vrati `AdSpacePackageDto` ili `null`" jer klijent (frontend/Postman) **eksplicitno** vidi je li resurs pronađen (404) ili je greška negdje drugdje.

`Optional<AdSpacePackage>.map(...)` — `Optional` ima svoj `map()`, sličan Stream-ovom — ako `Optional` sadrži vrijednost, primijeni funkciju i vrati novi `Optional` s rezultatom; ako je prazan, vrati prazan `Optional` bez greške.

## 9.3 Swagger / OpenAPI

`config/OpenApiConfig.java` + `@Tag`, `@Operation` anotacije na kontrolerima — `springdoc-openapi` biblioteka **automatski** čita sve tvoje `@RestController` klase, njihove `@GetMapping`/`@PostMapping` metode i anotacije, i generira **interaktivnu** web dokumentaciju na `/swagger-ui.html`. Ništa ručno ne pišeš za samu dokumentaciju — ona se izvlači direktno iz koda, pa je uvijek **sinkronizirana** sa stvarnim API-jem.

---

# DIO 10 — Thymeleaf, DUBOKO

## 10.1 Osnovni izrazi

```html
<p th:text="${pkg.name}">Placeholder</p>
```
`th:text` zamijeni sadržaj taga s vrijednošću izraza. `${pkg.name}` — Thymeleaf **navigacijski** izraz, ekvivalent Java koda `pkg.getName()` (Thymeleaf automatski zna pretvoriti `.name` u poziv gettera `getName()` — standardna JavaBean konvencija).

`Placeholder` tekst unutar taga je ono što se prikazuje **statički** ako otvoriš HTML fajl direktno u browseru (bez Springa) — korisno za dizajnere koji rade u čistom HTML-u. Kad Thymeleaf **stvarno** renderira stranicu kroz Spring, taj placeholder se **zamijeni** stvarnom vrijednosti.

```html
<div th:each="tier : ${tiers}">
    <span th:text="${tier.price}"></span>
</div>
```
`th:each` — petlja, ekvivalent Java `for (PricingTier tier : tiers) { ... }`. Div se **ponavlja** za svaki element u `tiers` listi.

```html
<div th:if="${pkg == null}">Package not found!</div>
```
`th:if` — uvjetno renderiranje, cijeli element se **potpuno izbaci** iz HTML-a ako je uvjet `false` (ne samo sakrije CSS-om — uopće ga nema u finalnom HTML-u).

## 10.2 Fragmenti — ponovna iskoristivost

```html
<!-- fragments/navbar.html -->
<nav th:fragment="navbar" class="ledvision-nav">
    ...
</nav>
```
```html
<!-- bilo koja druga stranica -->
<nav th:replace="~{fragments/navbar :: navbar}"></nav>
```
`th:fragment="navbar"` — označi dio HTML-a kao ponovno iskoristiv komad, s imenom "navbar". `th:replace="~{fragments/navbar :: navbar}"` — na drugoj stranici, **zamijeni** ovaj element cijelim tim fragmentom. Bez ovoga, morao bi kopirati-zalijepiti isti navbar HTML u SVAKU stranicu — ovako ga pišeš **jednom**.

## 10.3 URL generiranje

```html
<a th:href="@{/packages/{id}(id=${pkg.id})}">View</a>
```
`@{...}` — Thymeleaf URL izraz. Generira ispravan link, uključujući eventualni context path aplikacije (ako aplikacija nije na rootu servera). `{id}(id=${pkg.id})` — popuni path varijablu `{id}` vrijednošću `pkg.id`.

## 10.4 Spring Security integracija u template-ima

```html
<a th:href="@{/admin}" sec:authorize="hasRole('ADMIN')">Admin</a>
```
`thymeleaf-extras-springsecurity6` dependency (iz `pom.xml`) daje `sec:authorize` atribut — element se renderira **samo** ako trenutni prijavljeni korisnik ima danu ulogu/dozvolu. Ovo je **samo kozmetičko** (skriva link) — stvarna zaštita je uvijek u `SecurityConfig`-u (`.requestMatchers("/admin/**").hasRole("ADMIN")`). Nikad se ne oslanjaj SAMO na to da je nešto skriveno u UI-ju kao sigurnosnu mjeru!

---

# DIO 11 — Praktično: kako bi ovo napisao OD NULE (mini-tutorial)

Zamislimo da trebaš dodati **potpuno nov** entitet, npr. `Review` (recenzija paketa od strane kupca). Evo TOČNOG redoslijeda koraka koji bi napravio, isti koji sam ja slijedio za `PricingTier`/`AdExample`:

**Korak 1 — Entitet** (`model/Review.java`):
```java
@Entity
@Table(name = "reviews")
@Data @NoArgsConstructor @AllArgsConstructor
public class Review {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "package_id", nullable = false)
    private AdSpacePackage adSpacePackage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private Integer rating;  // 1-5

    @Column(length = 1000)
    private String comment;
}
```

**Korak 2 — Repository** (`repository/ReviewRepository.java`):
```java
@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByAdSpacePackageId(Long packageId);
}
```

**Korak 3 — DTO** (`dto/ReviewDto.java`):
```java
@Data @NoArgsConstructor @AllArgsConstructor
public class ReviewDto {
    private Long id;
    private Long packageId;
    private String username;
    private Integer rating;
    private String comment;
}
```

**Korak 4 — Mapper** (`mapper/ReviewMapper.java`):
```java
public class ReviewMapper {
    private ReviewMapper() {}
    public static ReviewDto toDto(Review r) {
        return new ReviewDto(r.getId(), r.getAdSpacePackage().getId(),
                r.getUser().getUsername(), r.getRating(), r.getComment());
    }
}
```

**Korak 5 — Servis** (`service/ReviewService.java`):
```java
@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewRepository reviewRepository;

    public List<Review> getReviewsForPackage(Long packageId) {
        return reviewRepository.findByAdSpacePackageId(packageId);
    }

    public void addReview(AdSpacePackage pkg, User user, int rating, String comment) {
        Review review = new Review();
        review.setAdSpacePackage(pkg);
        review.setUser(user);
        review.setRating(rating);
        review.setComment(comment);
        reviewRepository.save(review);
    }
}
```

**Korak 6 — Kontroler** (dodaj metodu u postojeći ili novi kontroler):
```java
@PostMapping("/packages/{id}/reviews")
public String addReview(@PathVariable Long id, @RequestParam int rating,
                        @RequestParam String comment, Authentication auth) {
    // dohvati pkg, dohvati usera iz auth, pozovi reviewService.addReview(...)
    return "redirect:/packages/" + id;
}
```

**Korak 7 — Template** — dodaj `th:each="review : ${reviews}"` blok u `packages/detail.html`.

**Korak 8 — Provjeri** `mvnw clean compile`, pokreni app, testiraj u browseru.

Ovo je **identičan** redoslijed koji smo koristili za `PricingTier` i `AdExample` tijekom refaktora. Nauči ovaj redoslijed napamet — to je "recept" za dodavanje bilo koje nove funkcionalnosti u Spring Boot aplikaciju s ovom arhitekturom.

---

# DIO 12 — Vjerojatna ispitna pitanja i model odgovori

**P: Objasni razliku između `@Component`, `@Service`, `@Repository`, `@Controller`.**
O: Sve četiri registriraju klasu kao Spring bean (sve su "specijalizacije" `@Component`-a). Razlikuju se po **semantičkoj namjeni** i nekim dodatnim ponašanjima: `@Repository` dodatno automatski prevodi bazu-specifične iznimke u Spring-ove generičke `DataAccessException` tipove; `@Service` i `@Controller` su čisto semantički markeri (za čitljivost i alate) bez dodatnog ponašanja iznad `@Component`.

**P: Što je razlika između `@Controller` i `@RestController`?**
O: `@RestController` = `@Controller` + `@ResponseBody` na svakoj metodi. `@Controller` vraća **ime view-a** koji Spring renderira (HTML preko Thymeleafa); `@RestController` vraća **podatke direktno** kao HTTP response body (obično JSON).

**P: Zašto koristiš DTO umjesto da vraćaš entitet direktno iz REST kontrolera?**
O: (vidi Dio 4.1) — sprječava beskonačne petlje/preveliki JSON zbog lazy relacija, kontrolira izloženost osjetljivih polja, izbjegava `LazyInitializationException`, i odvaja API ugovor od interne strukture baze.

**P: Objasni `@Transactional`.**
O: Metoda se izvršava unutar jedne baznе transakcije — sve operacije uspiju zajedno ili se sve poništi (rollback) ako se dogodi neuhvaćena iznimka. Garantira **atomičnost** — sprječava "napola izvršene" promjene u bazi.

**P: Što je `FetchType.LAZY` naspram `EAGER`, i zašto je LAZY zadano bolji izbor za `@ManyToOne`?**
O: LAZY odgađa dohvat povezanog entiteta dok se stvarno ne pristupi tom polju (dodatni SQL upit u tom trenutku); EAGER ga dohvati odmah. LAZY je bolji default za performanse (izbjegava nepotrebne upite), ali zahtijeva pažnju — pristup LAZY polju izvan aktivne JPA sesije baca `LazyInitializationException`, pa se koristi `JOIN FETCH` u repository upitu kad znaš da će ti trebati.

**P: Kako JWT osigurava da token nije lažiran?**
O: Treći dio tokena (signature) je kriptografski potpis prva dva dijela (header + payload), generiran tajnim ključem koji zna samo server. Ako netko promijeni sadržaj payloada, server pri validaciji izračuna potpis ponovno i vidi da se ne poklapa s onim u tokenu — token se odbija.

**P: Zašto imaš i access i refresh token?**
O: (vidi Dio 6.2) — access token kratkog vijeka za svakodnevne pozive (manja šteta ako se ukrade), refresh token dužeg vijeka samo za dobivanje novog access tokena bez ponovne prijave lozinkom.

**P: Objasni `mappedBy` u `@OneToMany`.**
O: Označava da ova strana relacije **nije** vlasnik foreign keya — kaže Hibernateu "foreign key je definiran na polju s tim imenom na DRUGOJ strani relacije, ne pravi dodatnu join tablicu ovdje".

**P: Što je `cascade = CascadeType.ALL` i `orphanRemoval`?**
O: `cascade = ALL` — operacije (save, delete, itd.) nad roditeljem se automatski prošire na sve povezane child entitete. `orphanRemoval = true` — ako child entitet ukloniš iz roditeljeve kolekcije (bez eksplicitnog brisanja), Hibernate ga sam obriše iz baze jer je ostao bez roditelja.

**P: Zašto se cijena kopira u `OrderItem.priceAtPurchase` umjesto da se uvijek čita iz `PricingTier`?**
O: (vidi Dio 2.6) — povijesne narudžbe moraju trajno pokazivati cijenu kakva je bila u trenutku kupnje, neovisno o budućim promjenama cijene tier-a.

**P: Objasni razliku između Servlet filtera i Spring event listenera.**
O: (vidi Dio 7.1) — filter presreće svaki HTTP zahtjev/odgovor i aktivno sudjeluje u obradi (može ga zaustaviti/promijeniti); listener reagira na već emitirane evente, sporedno/nakon što se nešto dogodilo, tipično bez utjecaja na tok trenutnog zahtjeva.

**P: Kako `@Async` metoda stvarno postane asinkrona — mora li postojati nešto drugo u konfiguraciji?**
O: Da — potreban je `@EnableAsync` u nekoj `@Configuration`/glavnoj klasi. Spring tada za `@Async` metode koristi poseban `TaskExecutor` (thread pool) da ih izvrši na drugoj niti, umjesto na niti koja poziva metodu.

---

# DIO 13 — Pojmovnik (brzi lookup)

- **Bean** — objekt kojim upravlja Spring IoC kontejner
- **DI (Dependency Injection)** — Spring automatski "ubrizgava" ovisnosti (druge bean-ove) u tvoje klase
- **ORM** — Object-Relational Mapping, pretvorba Java objekata ↔ redovi u bazi
- **Entity** — Java klasa mapirana na tablicu u bazi (`@Entity`)
- **Repository** — sloj odgovoran za razgovor s bazom podataka
- **DTO** — objekt čija je jedina svrha prenošenje podataka (bez logike, bez JPA anotacija)
- **JPQL** — upitni jezik sličan SQL-u, ali radi nad Java klasama/poljima umjesto tablica/stupaca
- **Lazy/Eager loading** — kad se povezani podatak dohvaća: kasnije na zahtjev (lazy) ili odmah (eager)
- **JWT** — JSON Web Token, samostojeći potpisan token za stateless autentikaciju
- **Filter chain** — niz filtera kroz koje HTTP zahtjev prolazi prije nego stigne do kontrolera
- **SecurityContext** — Spring Security-in spremnik trenutne autentikacije za trenutnu nit
- **Transactional** — grupa baznih operacija koje uspiju zajedno ili se sve ponište
- **Stateless** — server ne pamti stanje klijenta između zahtjeva
- **Cascade** — automatsko širenje operacije s roditeljskog na povezane entitete

---

Sad kad imaš ovo kao referencu — idemo **živo** kroz kod u chatu, dio po dio, počevši od Dijela 2 (domenski model), pošto smo Dio 1 (entry point) već prošli. Otvori `model/Location.java` u IntelliJ-u i javi kad si spreman.
