import { Injectable, signal } from '@angular/core';

export type Lang = 'en' | 'ar';
const LANG_KEY = 'rh.lang';

const STRINGS: Record<Lang, Record<string, string>> = {
  en: {
    'app.name': 'Ryde',
    'auth.login': 'Sign in',
    'auth.register': 'Create account',
    'auth.email': 'Email',
    'auth.password': 'Password',
    'auth.name': 'Full name',
    'auth.phone': 'Phone',
    'auth.otp': 'Verification code',
    'auth.verify': 'Verify email',
    'auth.resend': 'Resend code',
    'auth.noAccount': "Don't have an account?",
    'auth.hasAccount': 'Already have an account?',
    'auth.asDriver': 'Sign up as a driver',
    'home.whereTo': 'Where to?',
    'home.pickup': 'Pickup',
    'home.dropoff': 'Dropoff',
    'home.confirmPickup': 'Confirm pickup',
    'home.setDropoff': 'Set dropoff',
    'home.requestRide': 'Request ride',
    'home.findingDriver': 'Finding your driver',
    'home.driverOnWay': 'Driver on the way',
    'home.onTrip': 'On trip',
    'home.cancelRide': 'Cancel ride',
    'home.promo': 'Promo code',
    'home.apply': 'Apply',
    'home.eta': 'min away',
    'trip.history': 'Trips',
    'trip.replay': 'Replay trip',
    'trip.receipt': 'Receipt',
    'trip.empty': 'No trips yet. Your rides will appear here.',
    'profile.title': 'Profile',
    'profile.save': 'Save changes',
    'profile.changePhoto': 'Change photo',
    'profile.rating': 'Rating',
    'profile.member': 'Member since',
    'nav.home': 'Home',
    'nav.trips': 'Trips',
    'nav.profile': 'Profile',
    'nav.logout': 'Log out',
    'common.loading': 'Loading',
    'common.error': 'Something went wrong',
    'common.retry': 'Retry',
    'common.back': 'Back',
  },
  ar: {
    'app.name': 'رايد',
    'auth.login': 'تسجيل الدخول',
    'auth.register': 'إنشاء حساب',
    'auth.email': 'البريد الإلكتروني',
    'auth.password': 'كلمة المرور',
    'auth.name': 'الاسم الكامل',
    'auth.phone': 'رقم الهاتف',
    'auth.otp': 'كود التحقق',
    'auth.verify': 'تأكيد البريد',
    'auth.resend': 'إعادة إرسال الكود',
    'auth.noAccount': 'ليس لديك حساب؟',
    'auth.hasAccount': 'لديك حساب بالفعل؟',
    'auth.asDriver': 'سجّل كسائق',
    'home.whereTo': 'إلى أين؟',
    'home.pickup': 'نقطة الانطلاق',
    'home.dropoff': 'الوجهة',
    'home.confirmPickup': 'تأكيد الانطلاق',
    'home.setDropoff': 'حدد الوجهة',
    'home.requestRide': 'اطلب الرحلة',
    'home.findingDriver': 'جاري البحث عن سائق',
    'home.driverOnWay': 'السائق في الطريق',
    'home.onTrip': 'في الرحلة',
    'home.cancelRide': 'إلغاء الرحلة',
    'home.promo': 'كود الخصم',
    'home.apply': 'تطبيق',
    'home.eta': 'دقيقة',
    'trip.history': 'الرحلات',
    'trip.replay': 'إعادة الرحلة',
    'trip.receipt': 'الإيصال',
    'trip.empty': 'لا توجد رحلات بعد. ستظهر رحلاتك هنا.',
    'profile.title': 'الملف الشخصي',
    'profile.save': 'حفظ التعديلات',
    'profile.changePhoto': 'تغيير الصورة',
    'profile.rating': 'التقييم',
    'profile.member': 'عضو منذ',
    'nav.home': 'الرئيسية',
    'nav.trips': 'الرحلات',
    'nav.profile': 'حسابي',
    'nav.logout': 'تسجيل الخروج',
    'common.loading': 'جاري التحميل',
    'common.error': 'حدث خطأ ما',
    'common.retry': 'إعادة المحاولة',
    'common.back': 'رجوع',
  },
};

@Injectable({ providedIn: 'root' })
export class I18nService {
  readonly lang = signal<Lang>(this.initial());

  private initial(): Lang {
    try {
      const saved = localStorage.getItem(LANG_KEY);
      if (saved === 'ar' || saved === 'en') return saved;
    } catch {}
    return 'en';
  }

  t(key: string): string {
    return STRINGS[this.lang()][key] ?? key;
  }

  set(lang: Lang): void {
    this.lang.set(lang);
    document.documentElement.setAttribute('lang', lang);
    document.documentElement.setAttribute('dir', lang === 'ar' ? 'rtl' : 'ltr');
    try {
      localStorage.setItem(LANG_KEY, lang);
    } catch {}
  }

  toggle(): void {
    this.set(this.lang() === 'en' ? 'ar' : 'en');
  }

  apply(): void {
    this.set(this.lang());
  }
}
