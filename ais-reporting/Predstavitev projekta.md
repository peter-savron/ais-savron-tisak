# AIS projekt

Številka skupine: 6

Izbran tip projekta: B (agentni razvoj kode)

# Opis teme

Glavna težava, ki jo bomo tekom projekta reševali, je predelava specifikacij za namen razvoja kode oz. spec-driven design. 
Osredotočili se bomo dve tematiki: kako preveriti in izboljšati kakovost specifikacij, 
ter kako posredovati specifikacije agentu LLM, da bo njegovo delovanje čim bolj učinkovito.

## Kvaliteta specifikacij

Kvaliteto specifikacij lahko ocenimo na podlagi več lastnosti:

- izčrpnost specifikacije:
  - ali specifikacija opisuje vse lastnosti končnega produkta/procesa;
  - ali specifikacija opredeli ravnanje aplikacije v vseh primerih (pozitivni, alternativni in negativni scenarij)
  - ali specifikacija opredeli ravnanje vseh vpletenih akterjev (torej uporabnika FE, BE, integracij itd.);
  - ali so predvideni rezultati konkretno opredeljeni (ali se uporablja merljive metrike, ali splošne izraze);
- strukturiranost specifikacije:
    - ali se znotraj specifikacije sklicujemo na obstoječe dele specifikacije (ali se sklicujemo na splošna pravila za prikazovanje datumov, številk);
    - ali se določene definicije znotraj specifikacije ponavljajo (na več krajih je specificirano, kako naj se datumi zapisujejo);
    - ali je specifikacija smiselno in dosledno ločena po področjih (na podlage *feature*-jev, namembnostjo specifikacij, ipd.);
    - kolikšen obseg specifikacije moramo pregledati, da pridobimo vse potrebne informacije za implementacijo; 
- konsistentnost specifikacije:
  - ali se znotraj specifikacije dosledno uporabljajo isti izrazi;
  - ali se specifikacija sklada z implementirano rešitvijo;
- sledljivost sprememb:
  - ali lahko hitro ugotovimo, katere spremembe so bile narejene znotraj specifikacije;
  - ali lahko ugotovimo v sklopu katere delovne naloge (ticket-a, task-a) moramo določene spremembe opraviti;

## Pristop k reševanju problema

### Predelava originalnih specifikacij

Izvorno specifikacijo bomo s pomočjo agentov LLM, in orodij za pretvorbo dokumentov v Markdown predelali v krajše MD strani.
Te strani naj bi bile idealne velikosti za vlaganje besedila (torej 500-1000 žetonov), bi vsebovale sklice na druge relevantne strani iz specifikacije.
Med predelavo specifikacije v strni bi odpravili podvajanje definiciji prek uporabe sklicev na obstoječo definicijo,
označili dele specifikacije, ki so nejasni, in uskladili rabo tehničnih izrazov.

### Priprava arhitekture tehnične rešitve

Obstoječe rešitve se predela v vozlišča grafa arhitekture sistema. Vsako vozlišče ustreza vzorcu.
V vzorcu se opredeli, katere povezave/lastnosti mora ali ne sme vsebina tega vozlišča imeti, katere pa so pogojne oziroma neobvezne.
Vzorce lahko uporabnik sam definira. Na tem nivoju se še dodatno izpili strukturo specifikacije in preveri izčrpnost specifikacije
(če specifikacija ni izčrpna, ne bomo morali izpolniti vzorca). Vozlišča tega sloja bodo še bolj gosto povezana med sabo kot deli specifikacij
na podlagi vloge, ki imajo v arhitekturi sistema. Vozlišča bodo hkrati vezni člen med specifikacijo in implementacijo.
Glavni izziv na tej plasti bo pripraviti neko strukturo, ki je hkrati bolj strukturirana od Markdown-a, hkrati pa ni psevdokoda.

### Implementacija

Vsako vozlišče bomo implementirali v enem ali več programskih jezikih. Za vsak vzorec bo na razpolago možnost, 
da se priloži vzorcu primer pravilne implementacije (*code pattern*-a) za dan programski jezik in ogrodje.
Glavni cilj je, da je implementacija vzorca avtomatska in zahteva čim manjšo intervencijo s strani človeških uporabnikov.

### Tehnične specifikacije

Rešitev no implementirana kot CLI orodje. Orodje bomo implementirali prek uporabe ogrodja Quarkus, specifično knjižnice
**quarkus-picocli**. Za verzijoniranje specifikacij in kode bomo uporabljali že obstoječo orodje Git.
Trajno shranjevanje podatkov bomo implementirali preko datotečnega sistema in vložene podatkovne baze ArcadeDB, ki hkrati podpira
rabo relacijske podatkovne baze, grafovske podatkovne baze in vektorske podatkovne baze. Na podlagi zahtev, ki bodo nastale med razvojem, bomo agente implementirali
prek knjižnice **langchain4j** ali druge primerne knjižnice za razvoj agentov.

## Pričakovani rezultati

Aplikacija bo zmožna obstoječe specifikacije v več formatih (npr. pdf, md in docx) predelati v strani in graf znanja v obliki grafa arhitekture aplikacije,
ki ga lahko nato posredujemo notranjima li zunanjim agentom LLM. Kvaliteta obdelane specifikacije bo občutno boljša od začetnega vnosa.
Predelano specifikacijo lahko agenti kakovostno implementirajo. 
Delovanje aplikacije bomo preverili prek predelave dokumentacije odprtokodnih projektov ali dokumentacije drugih projektov na katerih člani skupine
delujejo v skladu s podpisanimi NDA-ji.