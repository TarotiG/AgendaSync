#!/usr/bin/env python3
"""
AgendaSync — Apple Calendar Duplicaten Verwijderaar
====================================================
Dit script verbindt via CalDAV met Apple Calendar, detecteert duplicate events
op basis van titel + startdatum, en verwijdert de duplicaten.

Vereisten:
    pip install caldav icalendar

Gebruik:
    python3 remove_apple_duplicates.py

Configuratie: vul je gegevens in bij de variabelen hieronder, of
stel de omgevingsvariabelen in (veiliger).
"""

import os
import sys
from collections import defaultdict
from dotenv import load_dotenv

try:
    import caldav
    from icalendar import Calendar
except ImportError:
    print("Installeer eerst de benodigde packages:")
    print("  pip install caldav icalendar")
    sys.exit(1)

# ─── Configuratie ─────────────────────────────────────────────────────────────
# Vul hier je gegevens in, of stel omgevingsvariabelen in
load_dotenv()

APPLE_USERNAME  = os.getenv("APPLE_USR")
APPLE_PASSWORD  = os.getenv("APPLE_SPEC_PW")
CALDAV_URL      = os.getenv("APPLE_CALDAV_URL")

# Zet op True om eerst alleen te tonen wat verwijderd zou worden (veilig testen)
DRY_RUN = False
# ──────────────────────────────────────────────────────────────────────────────


def get_event_key(vevent):
    """
    Maakt een unieke sleutel op basis van titel + startdatum.
    Events met dezelfde sleutel worden als duplicaat beschouwd.
    """
    summary = str(vevent.get("SUMMARY", "")).strip().lower()
    dtstart = vevent.get("DTSTART")
    start = str(dtstart.dt) if dtstart else "unknown"
    return f"{summary}|{start}"


def find_and_remove_duplicates():
    print(f"{'[DRY RUN] ' if DRY_RUN else ''}Verbinden met Apple Calendar...")
    print(f"Gebruiker: {APPLE_USERNAME}")
    print(f"URL: {CALDAV_URL}\n")

    # Verbind met CalDAV
    client = caldav.DAVClient(
        url=CALDAV_URL,
        username=APPLE_USERNAME,
        password=APPLE_PASSWORD,
    )

    principal = client.principal()
    calendars = principal.calendars()

    print(f"Gevonden kalenders: {len(calendars)}")
    for cal in calendars:
        print(f"  - {cal.name}")
    print()

    total_removed = 0

    for calendar in calendars:
        print(f"── Kalender: {calendar.name} ──────────────────────────")

        # Haal alle events op
        try:
            events = calendar.events()
        except Exception as e:
            print(f"  Fout bij ophalen events: {e}")
            continue

        print(f"  Totaal events: {len(events)}")

        # Groepeer events op sleutel (titel + startdatum)
        groups = defaultdict(list)
        for event in events:
            try:
                cal_data = Calendar.from_ical(event.data)
                for component in cal_data.walk():
                    if component.name == "VEVENT":
                        key = get_event_key(component)
                        uid = str(component.get("UID", ""))
                        summary = str(component.get("SUMMARY", "(geen titel)"))
                        dtstart = component.get("DTSTART")
                        start = str(dtstart.dt) if dtstart else "onbekend"
                        groups[key].append({
                            "event": event,
                            "uid": uid,
                            "summary": summary,
                            "start": start,
                        })
            except Exception as e:
                print(f"  Waarschuwing: kon event niet parsen: {e}")
                continue

        # Vind duplicaten
        duplicates_found = 0
        for key, group in groups.items():
            if len(group) <= 1:
                continue

            duplicates_found += 1
            summary = group[0]["summary"]
            start = group[0]["start"]
            print(f"\n  Duplicaat gevonden: '{summary}' op {start} ({len(group)}x)")

            # Houd de eerste, verwijder de rest
            to_keep = group[0]
            to_remove = group[1:]

            print(f"    Behouden: UID={to_keep['uid'][:20]}...")
            for item in to_remove:
                print(f"    Verwijderen: UID={item['uid'][:20]}...")
                if not DRY_RUN:
                    try:
                        item["event"].delete()
                        print(f"    ✓ Verwijderd")
                        total_removed += 1
                    except Exception as e:
                        print(f"    ✗ Fout bij verwijderen: {e}")
                else:
                    print(f"    [DRY RUN] Zou verwijderd worden")
                    total_removed += 1

        if duplicates_found == 0:
            print("  Geen duplicaten gevonden.")
        else:
            print(f"\n  {duplicates_found} duplicaat-groep(en) gevonden in '{calendar.name}'")

    print(f"\n{'─' * 50}")
    if DRY_RUN:
        print(f"[DRY RUN] {total_removed} event(s) zouden verwijderd worden.")
        print("Zet DRY_RUN = False om ze daadwerkelijk te verwijderen.")
    else:
        print(f"Klaar. {total_removed} duplicaat event(s) verwijderd.")


if __name__ == "__main__":
    find_and_remove_duplicates()