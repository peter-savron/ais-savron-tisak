# AIS projekt

Številka skupine: 6

Izbran tip projekta: B (agentni razvoj kode)

# Opis teme

Glavna težava, ki jo bomo s projektom reševali, je predelava specifikacij za razvoj kode oziroma razvoj, voden s specifikacijami (_spec-driven development_).
Osredotočili se bomo na dve tematiki: kako preveriti in izboljšati kakovost specifikacij ter kako posredovati specifikacije agentu LLM, da bo njegovo delovanje čim bolj učinkovito.

## Kvaliteta specifikacij

Kvaliteto specifikacij lahko ocenimo na podlagi več lastnosti:

- izčrpnost specifikacije:
  - ali specifikacija opisuje vse zahtevane lastnosti končnega produkta oziroma procesa;
  - ali specifikacija opredeli ravnanje aplikacije v vseh primerih (pozitivni, alternativni in negativni scenariji);
  - ali specifikacija opredeli ravnanje vseh vpletenih akterjev in komponent (uporabnika, FE, BE, integracij itd.);
  - ali so pričakovani rezultati konkretno opredeljeni oziroma preverljivi;
- strukturiranost specifikacije:
  - ali se znotraj specifikacije sklicujemo na obstoječe dele le te, na primer na splošna pravila za prikazovanje datumov in številk;
  - ali se določene definicije znotraj specifikacije ponavljajo na več mestih (npr. na več mestih piše format prikaza datumov)
  - ali je specifikacija smiselno in dosledno razdeljena po področjih, na primer po funkcionalnostih ali namenu;
  - kolikšen obseg specifikacije moramo pregledati, da pridobimo vse potrebne informacije za implementacijo;
- konsistentnost specifikacije:
  - ali se znotraj specifikacije dosledno uporabljajo isti izrazi;
  - ali si posamezne zahteve nasprotujejo;
  - ali se specifikacija sklada z implementirano rešitvijo;
- sledljivost sprememb:
  - ali lahko hitro ugotovimo, katere spremembe so bile narejene v specifikaciji;
  - ali lahko ugotovimo, v sklopu katere delovne naloge (_ticket_, _task_) moramo določene spremembe opraviti.

## Pristop k reševanju problema

### Predelava originalnih specifikacij

Izvorno specifikacijo bomo s pomočjo agentov LLM in orodij za pretvorbo dokumentov v Markdown predelali v krajše MD strani.
Te bodo pomensko zaključene enote z okvirno velikostjo 500-1000 žetonov in bodo vsebovale sklice na druge relevantne strani specifikacije.

Med predelavo specifikacije v strani bomo odpravili podvajanje definicij z uporabo sklicev na skupne definicije, označili nejasne dele specifikacije in uskladili rabo tehničnih izrazov. Manjkajoče informacije in vsebinske spremembe bo moral dopolniti oziroma potrditi uporabnik.

### Priprava arhitekture tehnične rešitve

Predelane specifikacije bomo preslikali v vozlišča grafa arhitekture sistema. Vsako vozlišče bo ustrezalo določenemu vzorcu.
V vzorcu bomo opredelili, katere povezave oziroma lastnosti mora ali ne sme imeti vozlišče, katere pa so pogojne oziroma neobvezne.

Vzorce bo lahko uporabnik definiral sam. Na tem nivoju bomo dodatno izpilili strukturo specifikacije in preverili njeno izčrpnost glede na zahteve izbranih vzorcev. Če bodo manjkale obvezne informacije, bomo to označili za dopolnitev. Izpolnjen vzorec sam po sebi še ne bo zagotavljal popolnosti poslovnih zahtev.

Vozlišča tega sloja bodo med seboj povezana glede na svojo vlogo v arhitekturi sistema. Hkrati bodo vezni člen med specifikacijo in implementacijo ter bodo ohranjala sklice na izvorne zahteve.
Glavni izziv na tej plasti bo pripraviti strukturo, ki je bolj strukturirana od Markdowna, vendar še ni psevdokoda.

### Implementacija

Vozlišča bomo s pomočjo agentov implementirali v enem ali več programskih jezikih. Za vsak vzorec bo mogoče priložiti primer pravilne implementacije (_code pattern_) za dani programski jezik in ogrodje.
Glavni cilj je, da je implementacija vzorca avtomatska in zahteva čim manj posegov uporabnikov.

Na izbranih funkcionalnostih bomo prikazali pot od specifikacije do implementacije in preverjanja z avtomatiziranimi testi, pripravljenimi na podlagi sprejemnih kriterijev.

### Tehnične specifikacije

Rešitev bo implementirana kot CLI orodje z uporabo ogrodja Quarkus, natančneje knjižnice **quarkus-picocli**. Za verzioniranje specifikacij in kode bomo uporabljali Git.
Za trajno shranjevanje podatkov bomo uporabili datotečni sistem in vloženo podatkovno bazo **ArcadeDB**, v kateri bomo hranili graf arhitekture in povezane podatke.

Na podlagi zahtev, ki bodo nastale med razvojem, bomo agente implementirali s knjižnico **LangChain4j** ali drugo primerno knjižnico za razvoj agentov.

```mermaid
flowchart TD
    U["Uporabnik"]@{ shape: person } <--> C
    subgraph APP["CLI aplikacija"]
        C["CLI: Quarkus/picocli"] <--> O["Orkestrator nalog"]
        O --> A["Agenti prek LangChain4j"]
        A --> T["Nadzorovana orodja"]
        M["MCP strežnik"] --> T
    end

    A <--> L["Jezikovni model"]

    O <-->|Stanje nalog| D["ArcadeDB"]
    T <-->|Graf in iskanje po dokumentaciji| D
    T <--> F["Dokumenti in koda: datotečni sistem in Git"]
    T --> V["Izolirano okolje za gradnjo in teste"]

    E["Zunanji agentni odjemalci"] --> M
```

## Pričakovani rezultati

Aplikacija bo omogočala predelavo obstoječih specifikacij v več formatih, na primer PDF, MD in DOCX, v povezane strani in graf znanja, ki predstavlja arhitekturo aplikacije. Pripravljeni kontekst bomo lahko posredovali notranjim ali zunanjim agentom LLM za implementacijo.

Strukturirana in povezana dokumentacija bo poenostavila tudi poizvedovanje po zahtevah, funkcionalnostih in njihovih medsebojnih povezavah. Tako pripravljen vir znanja bi bilo mogoče vključiti tudi v sisteme za pomoč uporabnikom, na primer za iskanje informacij in razlago pričakovanega vedenja aplikacije, s sklici na izvorno dokumentacijo.

Preverili bomo, ali predelava izboljša kakovost specifikacije in uspešnost implementacije. Pri tem bomo primerjali uporabo izvorne specifikacije in pripravljenega konteksta pri istem modelu, razvojni nalogi in sprejemnih testih. Merili bomo uspešnost testov, porabo žetonov, čas izvajanja in število potrebnih posegov uporabnika.

Delovanje aplikacije bomo preverili s predelavo dokumentacije odprtokodnih projektov ali dokumentacije drugih projektov, pri katerih sodelujejo člani skupine, ob upoštevanju dovoljenj za uporabo dokumentacije in podpisanih NDA-jev.
