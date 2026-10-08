'use client';

import { useEffect, useState } from 'react';
import Link from 'next/link';
import { Monitor, Users, Package, Calendar, ArrowRight, Clock, Building2, LogOut, Settings } from 'lucide-react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { useAuth } from '@/lib/auth-context';
import { requestsApi, resourcesApi, AllocationRequest, Resource } from '@/lib/api';
import { useRouter } from 'next/navigation';

const STATUS_LABELS: Record<string, { label: string; color: string }> = {
  SUBMITTED: { label: 'Złożony', color: 'bg-yellow-100 text-yellow-800' },
  APPROVED: { label: 'Zatwierdzony', color: 'bg-blue-100 text-blue-800' },
  ACTIVE: { label: 'Aktywny', color: 'bg-green-100 text-green-800' },
  COMPLETED: { label: 'Zakończony', color: 'bg-gray-100 text-gray-800' },
  REJECTED: { label: 'Odrzucony', color: 'bg-red-100 text-red-800' },
  CANCELLED: { label: 'Anulowany', color: 'bg-gray-100 text-gray-500' },
  OVERDUE: { label: 'Przeterminowane', color: 'bg-orange-100 text-orange-800' },
};

export default function DashboardPage() {
  const { user, loading: authLoading, logout, isManager, isItAdmin } = useAuth();
  const router = useRouter();
  const [myRequests, setMyRequests] = useState<AllocationRequest[]>([]);
  const [resources, setResources] = useState<Resource[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (authLoading) return;

    if (!user) {
      router.push('/login');
      return;
    }
    Promise.all([
      requestsApi.my().then(setMyRequests).catch(() => {}),
      resourcesApi.list().then(setResources).catch(() => {}),
    ]).finally(() => setLoading(false));
  }, [user, authLoading, router]);

  if (authLoading || !user) return null;

  const activeRequests = myRequests.filter(r => ['SUBMITTED', 'APPROVED', 'ACTIVE'].includes(r.status));
  const availableResources = resources.filter(r => r.available);

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Navbar */}
      <nav className="bg-white border-b px-6 py-3 flex items-center justify-between">
        <div className="flex items-center gap-2">
          <Building2 className="h-6 w-6 text-blue-600" />
          <span className="font-bold text-lg">DeskFlow</span>
        </div>
        <div className="flex items-center gap-4">
          <span className="text-sm text-gray-600">
            {user.firstName} {user.lastName} ({user.role})
          </span>
          <div className="flex gap-2">
            <Link href="/wnioski">
              <Button variant="ghost" size="sm">Moje wnioski</Button>
            </Link>
            <Link href="/zasoby">
              <Button variant="ghost" size="sm">Zasoby</Button>
            </Link>
            {isManager() && (
              <Link href="/administracja">
                <Button variant="ghost" size="sm">Wnioski</Button>
              </Link>
            )}
            {isItAdmin() && (
              <Link href="/admin/it">
                <Button variant="ghost" size="sm">IT Admin</Button>
              </Link>
            )}
            <Button variant="ghost" size="sm" onClick={logout}>
              <LogOut className="h-4 w-4" />
            </Button>
          </div>
        </div>
      </nav>

      <div className="p-8 max-w-7xl mx-auto">
        <div className="mb-8">
          <h1 className="text-2xl font-bold">Witaj, {user.firstName}!</h1>
          <p className="text-gray-600 mt-1">System zarządzania zasobami biurowymi</p>
        </div>

        {/* Stats */}
        <div className="grid gap-4 md:grid-cols-3 mb-8">
          <Card>
            <CardContent className="p-6">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm text-gray-600">Moje aktywne wnioski</p>
                  <p className="text-3xl font-bold mt-1">{activeRequests.length}</p>
                </div>
                <Calendar className="h-8 w-8 text-blue-500" />
              </div>
            </CardContent>
          </Card>
          <Card>
            <CardContent className="p-6">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm text-gray-600">Dostępne zasoby</p>
                  <p className="text-3xl font-bold mt-1">{availableResources.length}</p>
                </div>
                <Monitor className="h-8 w-8 text-green-500" />
              </div>
            </CardContent>
          </Card>
          <Card>
            <CardContent className="p-6">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm text-gray-600">Wszystkie zasoby</p>
                  <p className="text-3xl font-bold mt-1">{resources.length}</p>
                </div>
                <Package className="h-8 w-8 text-purple-500" />
              </div>
            </CardContent>
          </Card>
        </div>

        {/* Quick Actions */}
        <Card className="mb-6">
          <CardHeader>
            <CardTitle>Szybkie akcje</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="flex flex-wrap gap-3">
              <Link href="/zasoby">
                <Button className="gap-2">
                  <Monitor className="h-4 w-4" />
                  Przeglądaj zasoby i złóż wniosek
                </Button>
              </Link>
              <Link href="/wnioski">
                <Button variant="outline" className="gap-2">
                  <Clock className="h-4 w-4" />
                  Historia wniosków
                </Button>
              </Link>
              {isManager() && (
                <Link href="/administracja">
                  <Button variant="outline" className="gap-2">
                    <Users className="h-4 w-4" />
                    Panel administracji
                  </Button>
                </Link>
              )}
              {isItAdmin() && (
                <Link href="/admin/it">
                  <Button variant="outline" className="gap-2">
                    <Settings className="h-4 w-4" />
                    IT Admin
                  </Button>
                </Link>
              )}
            </div>
          </CardContent>
        </Card>

        {/* Active requests */}
        <Card>
          <CardHeader className="flex flex-row items-center justify-between">
            <div>
              <CardTitle>Moje aktywne wnioski</CardTitle>
              <CardDescription>Wnioski oczekujące i aktywne udostępnienia</CardDescription>
            </div>
            <Link href="/wnioski">
              <Button variant="outline" size="sm" className="gap-2">
                Wszystkie <ArrowRight className="h-4 w-4" />
              </Button>
            </Link>
          </CardHeader>
          <CardContent>
            {loading ? (
              <p className="text-gray-500 text-sm">Ładowanie...</p>
            ) : activeRequests.length === 0 ? (
              <p className="text-gray-500 text-sm">Brak aktywnych wniosków</p>
            ) : (
              <div className="space-y-3">
                {activeRequests.slice(0, 5).map(r => (
                  <div key={r.id} className="flex items-center justify-between p-3 bg-gray-50 rounded-lg">
                    <div>
                      <div className="flex items-center gap-2 mb-1">
                        <span className={`text-xs px-2 py-0.5 rounded-full font-medium ${STATUS_LABELS[r.status]?.color}`}>
                          {STATUS_LABELS[r.status]?.label}
                        </span>
                      </div>
                      <p className="text-sm font-medium">{r.dateFrom} → {r.dateTo}</p>
                      {r.justification && (
                        <p className="text-xs text-gray-500">{r.justification}</p>
                      )}
                    </div>
                  </div>
                ))}
              </div>
            )}
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
