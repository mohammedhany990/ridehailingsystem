export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
}

export interface Profile {
  id: string;
  name: string;
  email: string;
  phone: string | null;
  rating: number;
  role: string;
  avatarUrl: string | null;
  createdAt: string;
}

export type RideStatus = 'REQUESTED' | 'ACCEPTED' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED';

export interface Ride {
  id: string;
  riderId: string;
  driverId: string | null;
  status: RideStatus;
  vehicleType: string;
  pickupLat: number;
  pickupLng: number;
  dropoffLat: number;
  dropoffLng: number;
  requestedAt: string;
  acceptedAt: string | null;
  startedAt: string | null;
  completedAt: string | null;
  fare: Fare | null;
  proposedFare: number | null;
  negotiatedAmount: number | null;
  cancellation: { reason: string; cancelledBy: string; cancelledAt: string; cancellationFee: number | null } | null;
}

export interface Fare {
  baseFare: number;
  distanceKm: number;
  surgeMultiplier: number;
  totalAmount: number;
  currency: string;
}

export interface FareEstimate {
  estimatedMinFare: number;
  estimatedMaxFare: number;
  surgeMultiplier: number;
  currency: string;
  vehicleType: string;
  distanceKm: number;
  durationMinutes: number;
  promoCode: string | null;
  discountAmount: number | null;
}

export interface RouteInfo {
  distanceKm: number;
  durationMinutes: number;
  geometry: [number, number][] | null;
}

export interface TrailPoint {
  lat: number;
  lng: number;
  recordedAt: string;
}

export interface Trail {
  rideId: string;
  status: RideStatus;
  pointCount: number;
  points: TrailPoint[];
}

export interface AppNotification {
  id: string;
  type: string;
  title: string;
  body: string;
  read: boolean;
  createdAt: string;
}

export interface Payment {
  id: string;
  rideId: string;
  amount: number;
  currency: string;
  status: string;
  createdAt: string;
}

export interface ChargeResponse {
  id: string;
  rideId: string;
  amount: number;
  currency: string;
  status: string;
  createdAt: string;
  gateway: 'stripe' | 'simulated';
  clientSecret: string | null;
}

export interface PaymentConfig {
  gateway: 'stripe' | 'simulated';
  publishableKey: string | null;
}

export interface Receipt {
  receiptNumber: string;
  paymentId: string;
  rideId: string;
  amount: number;
  currency: string;
  status: string;
  paymentMethod: string;
  issuedAt: string;
}

export interface VehicleTier {
  type: string;
  label: string;
  description: string;
  icon: string;
  capacity: number;
}

export interface FareOffer {
  id: string;
  rideId: string;
  driverId: string;
  amount: number;
  currency: string;
  status: 'PENDING' | 'ACCEPTED' | 'DECLINED' | 'SUPERSEDED' | 'EXPIRED';
  createdAt: string;
}
