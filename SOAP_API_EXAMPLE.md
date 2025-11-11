# Example: Calling /api/fina/efiskalizacija/evidentirajERacun

## Endpoint
- **Method**: `POST`
- **URL**: `http://localhost:8080/api/fina/efiskalizacija/evidentirajERacun`
- **Content-Type**: `application/json`
- **Authentication**: Required (JWT Bearer token)

## Authentication
This endpoint requires authentication. You need to include a JWT token in the Authorization header:
```
Authorization: Bearer {your-jwt-token}
```

First, get a token by calling `/auth/login`:
```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "your-username", "password": "your-password"}'
```

## Example JSON Request Body

```json
{
  "vrstaERacuna": "I",
  "brojDokumenta": "INV-2024-0001",
  "datumIzdavanja": "2024-01-15",
  "vrstaDokumenta": "380",
  "valuta": "EUR",
  "izdavateljIme": "Moja Tvrtka d.o.o.",
  "izdavateljOib": "12345678901",
  "izdavateljOibOperatera": "12345678901",
  "primateljIme": "Kupac d.o.o.",
  "primateljOib": "98765432109",
  "ukupnoNeto": 1000.00,
  "ukupnoBezPdv": 1000.00,
  "ukupnoPdv": 250.00,
  "ukupnoSPdv": 1250.00,
  "iznosKojiDospijeva": 1250.00,
  "indikatorKopije": false,
  "stavke": [
    {
      "kolicina": 10,
      "jedinicaMjere": "C62",
      "neto": 1000.00,
      "artiklNetoCijena": 100.00,
      "artiklKategorijaPdv": "S",
      "artiklStopaPdv": 25.00,
      "artiklNaziv": "Proizvod A",
      "artiklOpis": "Opis proizvoda A"
    }
  ],
  "pdvRaspodjela": [
    {
      "kategorijaPdv": "S",
      "oporeziviIznos": 1000.00,
      "iznosPoreza": 250.00,
      "stopa": 25.00
    }
  ]
}
```

## cURL Example

```bash
curl -X POST http://localhost:8080/api/fina/efiskalizacija/evidentirajERacun \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN_HERE" \
  -d '{
    "vrstaERacuna": "I",
    "brojDokumenta": "INV-2024-0001",
    "datumIzdavanja": "2024-01-15",
    "vrstaDokumenta": "380",
    "valuta": "EUR",
    "izdavateljIme": "Moja Tvrtka d.o.o.",
    "izdavateljOib": "12345678901",
    "izdavateljOibOperatera": "12345678901",
    "primateljIme": "Kupac d.o.o.",
    "primateljOib": "98765432109",
    "ukupnoNeto": 1000.00,
    "ukupnoBezPdv": 1000.00,
    "ukupnoPdv": 250.00,
    "ukupnoSPdv": 1250.00,
    "iznosKojiDospijeva": 1250.00,
    "indikatorKopije": false,
    "stavke": [
      {
        "kolicina": 10,
        "jedinicaMjere": "C62",
        "neto": 1000.00,
        "artiklNetoCijena": 100.00,
        "artiklKategorijaPdv": "S",
        "artiklStopaPdv": 25.00,
        "artiklNaziv": "Proizvod A"
      }
    ],
    "pdvRaspodjela": [
      {
        "kategorijaPdv": "S",
        "oporeziviIznos": 1000.00,
        "iznosPoreza": 250.00,
        "stopa": 25.00
      }
    ]
  }'
```

## JavaScript/TypeScript Example (fetch)

```javascript
const response = await fetch('http://localhost:8080/api/fina/efiskalizacija/evidentirajERacun', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${yourJwtToken}`,
  },
  body: JSON.stringify({
    vrstaERacuna: 'I',
    brojDokumenta: 'INV-2024-0001',
    datumIzdavanja: '2024-01-15',
    vrstaDokumenta: '380',
    valuta: 'EUR',
    izdavateljIme: 'Moja Tvrtka d.o.o.',
    izdavateljOib: '12345678901',
    izdavateljOibOperatera: '12345678901',
    primateljIme: 'Kupac d.o.o.',
    primateljOib: '98765432109',
    ukupnoNeto: 1000.00,
    ukupnoBezPdv: 1000.00,
    ukupnoPdv: 250.00,
    ukupnoSPdv: 1250.00,
    iznosKojiDospijeva: 1250.00,
    indikatorKopije: false,
    stavke: [
      {
        kolicina: 10,
        jedinicaMjere: 'C62',
        neto: 1000.00,
        artiklNetoCijena: 100.00,
        artiklKategorijaPdv: 'S',
        artiklStopaPdv: 25.00,
        artiklNaziv: 'Proizvod A'
      }
    ],
    pdvRaspodjela: [
      {
        kategorijaPdv: 'S',
        oporeziviIznos: 1000.00,
        iznosPoreza: 250.00,
        stopa: 25.00
      }
    ]
  })
});

const result = await response.json();
console.log(result);
```

## Java Example (RestTemplate)

```java
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

RestTemplate restTemplate = new RestTemplate();
HttpHeaders headers = new HttpHeaders();
headers.setContentType(MediaType.APPLICATION_JSON);
headers.setBearerAuth("YOUR_JWT_TOKEN_HERE");

Map<String, Object> requestBody = Map.of(
    "vrstaERacuna", "I",
    "brojDokumenta", "INV-2024-0001",
    "datumIzdavanja", "2024-01-15",
    "vrstaDokumenta", "380",
    "valuta", "EUR",
    "izdavateljIme", "Moja Tvrtka d.o.o.",
    "izdavateljOib", "12345678901",
    "izdavateljOibOperatera", "12345678901",
    "primateljIme", "Kupac d.o.o.",
    "primateljOib", "98765432109",
    "ukupnoNeto", new BigDecimal("1000.00"),
    "ukupnoBezPdv", new BigDecimal("1000.00"),
    "ukupnoPdv", new BigDecimal("250.00"),
    "ukupnoSPdv", new BigDecimal("1250.00"),
    "iznosKojiDospijeva", new BigDecimal("1250.00"),
    "indikatorKopije", false,
    "stavke", List.of(
        Map.of(
            "kolicina", new BigDecimal("10"),
            "jedinicaMjere", "C62",
            "neto", new BigDecimal("1000.00"),
            "artiklNetoCijena", new BigDecimal("100.00"),
            "artiklKategorijaPdv", "S",
            "artiklStopaPdv", new BigDecimal("25.00"),
            "artiklNaziv", "Proizvod A"
        )
    ),
    "pdvRaspodjela", List.of(
        Map.of(
            "kategorijaPdv", "S",
            "oporeziviIznos", new BigDecimal("1000.00"),
            "iznosPoreza", new BigDecimal("250.00"),
            "stopa", new BigDecimal("25.00")
        )
    )
);

HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
ResponseEntity<Object> response = restTemplate.postForEntity(
    "http://localhost:8080/api/fina/efiskalizacija/evidentirajERacun",
    request,
    Object.class
);
```

## Field Descriptions

### Required Fields:
- **vrstaERacuna**: "I" (Izlazni), "U" (Ulazni), or "IR" (Izlazni bez eRačuna)
- **brojDokumenta**: Document number
- **datumIzdavanja**: Date in format `YYYY-MM-DD`
- **vrstaDokumenta**: UNTDID 1001 code (e.g., "380" for invoice)
- **valuta**: ISO 4217 currency code (e.g., "EUR", "HRK")
- **izdavateljIme**: Issuer company name
- **izdavateljOib**: Issuer OIB (11 digits)
- **izdavateljOibOperatera**: Operator OIB (11 digits)
- **primateljIme**: Recipient company name
- **primateljOib**: Recipient OIB (11 digits)
- **ukupnoNeto**: Total net amount
- **ukupnoBezPdv**: Total without VAT
- **ukupnoPdv**: Total VAT amount
- **ukupnoSPdv**: Total with VAT
- **iznosKojiDospijeva**: Amount due
- **stavke**: At least 1 item
- **pdvRaspodjela**: At least 1 VAT row

### Field Codes:
- **jedinicaMjere**: EN16931 unit code (e.g., "C62" for pieces)
- **artiklKategorijaPdv**: UNCL5305 VAT category (e.g., "S" for Standard rate, "AE" for VAT exempt)
- **kategorijaPdv**: Same as above for VAT distribution

## Response

The response will be a SOAP response object from FINA containing:
- Status information
- Confirmation or error details
- Any transaction IDs

The XML files will be automatically saved to:
- `fiskalizacija/request-{id}.xml` - The signed SOAP request
- `fiskalizacija/response-{id}.xml` - The SOAP response

