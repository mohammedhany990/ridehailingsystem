import {
  AfterViewInit, Component, ElementRef, EventEmitter, NgZone, OnDestroy, Output, ViewChild, inject,
} from '@angular/core';
import * as L from 'leaflet';
import { ThemeService } from '../../core/theme.service';

const CAIRO: L.LatLngExpression = [30.0444, 31.2357];

@Component({
  selector: 'app-map',
  standalone: true,
  template: '<div #host class="map-host" role="application" aria-label="map"></div>',
  styles: `
    .map-host { position: absolute; inset: 0; z-index: 1; }
    :host { display: block; position: absolute; inset: 0; }
  `,
})
export class MapComponent implements AfterViewInit, OnDestroy {
  @ViewChild('host', { static: true }) host!: ElementRef<HTMLDivElement>;
  @Output() tapped = new EventEmitter<{ lat: number; lng: number }>();

  private theme = inject(ThemeService);
  private zone = inject(NgZone);

  private map!: L.Map;
  private tileLayer!: L.TileLayer;
  private pickupMarker?: L.Marker;
  private dropoffMarker?: L.Marker;
  private driverMarker?: L.Marker;
  private routeLine?: L.Polyline;
  private trailLine?: L.Polyline;
  private themeSub?: ReturnType<typeof setInterval>;
  private lastTheme = '';

  ngAfterViewInit(): void {
    this.zone.runOutsideAngular(() => {
      this.map = L.map(this.host.nativeElement, {
        center: CAIRO,
        zoom: 13,
        zoomControl: false,
        attributionControl: true,
      });
      this.setTiles();
      this.map.on('click', (e: L.LeafletMouseEvent) => {
        this.tapped.emit({ lat: e.latlng.lat, lng: e.latlng.lng });
      });
      L.control.zoom({ position: 'bottomright' }).addTo(this.map);
    });
    this.lastTheme = this.theme.theme();
    this.themeSub = setInterval(() => {
      if (this.theme.theme() !== this.lastTheme) {
        this.lastTheme = this.theme.theme();
        this.setTiles();
      }
    }, 400);
  }

  private setTiles(): void {
    const dark = this.theme.theme() === 'dark';
    const url = dark
      ? 'https://{s}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}{r}.png'
      : 'https://{s}.basemaps.cartocdn.com/light_all/{z}/{x}/{y}{r}.png';
    if (this.tileLayer) this.tileLayer.remove();
    this.tileLayer = L.tileLayer(url, {
      maxZoom: 19,
      attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> &copy; <a href="https://carto.com/">CARTO</a>',
    }).addTo(this.map);
  }

  private pinIcon(kind: 'pickup' | 'dropoff'): L.DivIcon {
    const color = kind === 'pickup' ? '#22d3a7' : '#ff5d73';
    return L.divIcon({
      className: '',
      html: `<div style="width:18px;height:18px;border-radius:50% 50% 50% 0;transform:rotate(-45deg);background:${color};border:3px solid #fff;box-shadow:0 4px 12px rgba(0,0,0,.4)"></div>`,
      iconSize: [18, 18],
      iconAnchor: [9, 16],
    });
  }

  private carIcon(): L.DivIcon {
    return L.divIcon({
      className: '',
      html: `<div style="width:38px;height:38px;border-radius:50%;background:#4f7cff;border:3px solid #fff;box-shadow:0 6px 18px rgba(0,0,0,.45);display:grid;place-items:center">
        <svg viewBox="0 0 24 24" width="20" height="20" fill="#fff"><path d="M5 11l1.5-4.5A2 2 0 0 1 8.4 5h7.2a2 2 0 0 1 1.9 1.5L19 11m-14 0h14a2 2 0 0 1 2 2v4h-2.2m-13.6 0H3v-4a2 2 0 0 1 2-2Zm.2 6a1.8 1.8 0 1 0 0-.01ZM18.8 17a1.8 1.8 0 1 0 0-.01Z"/></svg>
      </div>`,
      iconSize: [38, 38],
      iconAnchor: [19, 19],
    });
  }

  setPickup(lat: number, lng: number): void {
    if (this.pickupMarker) this.pickupMarker.setLatLng([lat, lng]);
    else this.pickupMarker = L.marker([lat, lng], { icon: this.pinIcon('pickup') }).addTo(this.map);
  }

  setDropoff(lat: number, lng: number): void {
    if (this.dropoffMarker) this.dropoffMarker.setLatLng([lat, lng]);
    else this.dropoffMarker = L.marker([lat, lng], { icon: this.pinIcon('dropoff') }).addTo(this.map);
  }

  setDriver(lat: number, lng: number): void {
    if (this.driverMarker) this.driverMarker.setLatLng([lat, lng]);
    else this.driverMarker = L.marker([lat, lng], { icon: this.carIcon(), zIndexOffset: 500 }).addTo(this.map);
  }

  clearDriver(): void {
    this.driverMarker?.remove();
    this.driverMarker = undefined;
  }

  drawRoute(coords: [number, number][] | null): void {
    this.routeLine?.remove();
    this.routeLine = undefined;
    if (!coords?.length) return;
    const latlngs = coords.map(([lng, lat]) => [lat, lng] as [number, number]);
    this.routeLine = L.polyline(latlngs, {
      color: '#4f7cff', weight: 5, opacity: 0.9, lineCap: 'round', lineJoin: 'round',
    }).addTo(this.map);
  }

  drawTrail(points: { lat: number; lng: number }[], progress = 1): void {
    this.trailLine?.remove();
    this.trailLine = undefined;
    if (!points.length) return;
    const upto = Math.max(1, Math.ceil(points.length * progress));
    const latlngs = points.slice(0, upto).map((p) => [p.lat, p.lng] as [number, number]);
    this.trailLine = L.polyline(latlngs, {
      color: '#22d3a7', weight: 4, opacity: 0.85, dashArray: '1 8', lineCap: 'round',
    }).addTo(this.map);
  }

  fitAll(): void {
    const layers: L.LatLng[] = [];
    for (const m of [this.pickupMarker, this.dropoffMarker, this.driverMarker]) {
      if (m) layers.push(m.getLatLng());
    }
    if (this.routeLine) layers.push(...(this.routeLine.getLatLngs() as L.LatLng[]));
    if (!layers.length) return;
    this.map.flyToBounds(L.latLngBounds(layers).pad(0.25), { duration: 0.6 });
  }

  invalidate(): void {
    this.map?.invalidateSize();
  }

  ngOnDestroy(): void {
    if (this.themeSub) clearInterval(this.themeSub);
    this.map?.remove();
  }
}
