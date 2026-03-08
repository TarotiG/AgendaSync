#!/usr/bin/env python3
"""
AgendaSync — Google Calendar Duplicaten Verwijderaar
=====================================================
Dit script verbindt via een Service Account met Google Calendar, detecteert
duplicate events op basis van titel + startdatum, en verwijdert de duplicaten.

Vereisten:
    pip install google-auth google-auth-httplib2 google-api-python-client

Gebruik:
    python3 remove_google_duplicates.py

Configuratie: vul je gegevens in bij de variabelen hieronder, of
stel de omgevingsvariabelen in (veiliger).
"""

import os
import sys
from collections import defaultdict

try:
    from google.oauth2 import service_account
    from googleapiclient.discovery import build
    import google.auth.transport.requests
except ImportError:
    print("Installeer eerst de benodigde packages:")
    print("  pip install google-auth google-auth-httplib2 google-api-python-client")
    sys.exit(1)

# ─── Configuratie ─────────────────────────────────────────────────────────────
# Pad naar je service account JSON bestand
SERVICE_ACCOUNT_FILE = os.environ.get(
    "GOOGLE_SERVICE_ACCOUNT_FILE",
    "agendasync-474013-82a844cdf0f6.json"
)

# De kalender om te controleren — "primary" is je hoofdkalender
CALENDAR_ID = "primary"

# Zet op True om eerst alleen te tonen wat verwijderd zou worden (veilig testen)
DRY_RUN = True
# ──────────────────────────────────────────────────────────────────────────────

SCOPES = ["https://www.googleapis.com/auth/calendar"]


def get_event_key(event):
    """
    Maakt een unieke sleutel op basis van titel + startdatum.
    Events met dezelfde sleutel worden als duplicaat beschouwd.
    """
    summary = event.get("summary", "").strip().lower()

    start = event.get("start", {})
    # all-day events gebruiken "date", tijdgebonden events gebruiken "dateTime"
    start_str = start.get("dateTime") or start.get("date") or "unknown"

    # Normaliseer: verwijder tijdzone suffix voor vergelijking
    # "2026-04-24T10:00:00+02:00" en "2026-04-24T08:00:00Z" zijn hetzelfde moment
    if "T" in start_str:
        # Converteer naar UTC voor eerlijke vergelijking
        from datetime import datetime, timezone
        try:
            from dateutil import parser as dateutil_parser
            dt = dateutil_parser.parse(start_str).astimezone(timezone.utc)
            start_str = dt.strftime("%Y-%m-%dT%H:%M:%SZ")
        except Exception:
            pass  # Als parsing faalt, gebruik de ruwe string

    return f"{summary}|{start_str}"


def find_and_remove_duplicates():
    print(f"{'[DRY RUN] ' if DRY_RUN else ''}Verbinden met Google Calendar...")
    print(f"Service account: {SERVICE_ACCOUNT_FILE}")
    print(f"Kalender: {CALENDAR_ID}\n")

    # Authenticeer via service account
    if not os.path.exists(SERVICE_ACCOUNT_FILE):
        print(f"Fout: service account bestand niet gevonden: {SERVICE_ACCOUNT_FILE}")
        print("Stel GOOGLE_SERVICE_ACCOUNT_FILE in als omgevingsvariabele, of")
        print("zet het JSON bestand in dezelfde map als dit script.")
        sys.exit(1)

    credentials = service_account.Credentials.from_service_account_file(
        SERVICE_ACCOUNT_FILE,
        scopes=SCOPES,
    )

    service = build("calendar", "v3", credentials=credentials)

    # Haal alle events op (met paginering)
    print("Events ophalen...")
    all_events = []
    page_token = None

    while True:
        response = service.events().list(
            calendarId=CALENDAR_ID,
            singleEvents=True,
            orderBy="startTime",
            maxResults=2500,
            pageToken=page_token,
        ).execute()

        items = response.get("items", [])
        all_events.extend(items)
        page_token = response.get("nextPageToken")

        if not page_token:
            break

    print(f"Totaal events opgehaald: {len(all_events)}\n")

    # Groepeer op sleutel (titel + startdatum)
    groups = defaultdict(list)
    for event in all_events:
        # Sla geannuleerde events over
        if event.get("status") == "cancelled":
            continue
        key = get_event_key(event)
        groups[key].append(event)

    # Vind en verwijder duplicaten
    duplicates_found = 0
    total_removed = 0

    for key, group in groups.items():
        if len(group) <= 1:
            continue

        duplicates_found += 1
        summary = group[0].get("summary", "(geen titel)")
        start = group[0].get("start", {})
        start_str = start.get("dateTime") or start.get("date") or "onbekend"

        print(f"Duplicaat gevonden: '{summary}' op {start_str} ({len(group)}x)")

        # Sorteer op aanmaakdatum — houd de oudste (originele) event
        group_sorted = sorted(group, key=lambda e: e.get("created", ""))
        to_keep = group_sorted[0]
        to_remove = group_sorted[1:]

        print(f"  Behouden:    ID={to_keep['id'][:20]}... (aangemaakt: {to_keep.get('created', '?')[:19]})")

        for item in to_remove:
            print(f"  Verwijderen: ID={item['id'][:20]}... (aangemaakt: {item.get('created', '?')[:19]})")
            if not DRY_RUN:
                try:
                    service.events().delete(
                        calendarId=CALENDAR_ID,
                        eventId=item["id"]
                    ).execute()
                    print(f"  ✓ Verwijderd")
                    total_removed += 1
                except Exception as e:
                    print(f"  ✗ Fout bij verwijderen: {e}")
            else:
                print(f"  [DRY RUN] Zou verwijderd worden")
                total_removed += 1

        print()

    print("─" * 50)
    if duplicates_found == 0:
        print("Geen duplicaten gevonden.")
    elif DRY_RUN:
        print(f"[DRY RUN] {total_removed} event(s) zouden verwijderd worden ({duplicates_found} duplicaat-groep(en)).")
        print("Zet DRY_RUN = False om ze daadwerkelijk te verwijderen.")
    else:
        print(f"Klaar. {total_removed} duplicaat event(s) verwijderd ({duplicates_found} duplicaat-groep(en)).")


if __name__ == "__main__":
    find_and_remove_duplicates()