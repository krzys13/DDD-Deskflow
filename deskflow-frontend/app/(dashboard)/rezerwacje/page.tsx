'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import Link from 'next/link';
import { ArrowLeft, XCircle, Box, Calendar, MapPin } from 'lucide-react';
import { Card, CardContent } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { useAuth } from '@/lib/auth-context';
import { requestsApi, resourcesApi, AllocationRequest, Resource } from '@/lib/api';
import { useToast } from '@/hooks/use-toast';

const STATUS_LABELS: Record<string, { label: string; color: string }> = {
  SUBMITTED: { label: 'Złożony', color: 'bg-yellow-100 text-yellow-800' },
  APPROVED: { label: 'Zatwierdzony', color: 'bg-blue-100 text-blue-800' },
  ACTIVE: { label: 'Aktywny', color: 'bg-green-100 text-green-800' },
  COMPLETED: { label: 'Zakończony', color: 'bg-gray-100 text-gray-700' },
  REJECTED: { label: 'Odrzucony', color: 'bg-red-100 text-red-800' },
  CANCELLED: { label: 'Anulowany', color: 'bg-gray-100 text-gray-500' },
  OVERDUE: { label: 'Przeterminowane', color: 'bg-orange-100 text-orange-800' },
};

const TYPE_LABELS: Record<string, string> = {
  DESK: 'Biurko', CONFERENCE_ROOM: 'Sala konf.', LAPTOP: 'Laptop',
  MONITOR: 'Monitor', PROJECTOR: 'Projektor', VEHICLE: 'Pojazd', ACCESSORY: 'Akcesoria',
};

export default function MyRequestsPage() {
  const { user, loading: authLoading } = useAuth();
  const router = useRouter();
  const { toast } = useToast();
  const [requests, setRequests] = useState<AllocationRequest[]>([]);
  const [resourceMap, setResourceMap] = useState<Record<string, Resource>>({});
  const [loading, setLoading] = useState(true);

  const load = async () => {
    try {
      const [reqs, resources] = await Promise.all([requestsApi.my(), resourcesApi.list()]);
      setRequests(reqs);
      setResourceMap(Object.fromEntries(resources.map(r => [r.id, r])));
    } catch (e: unknown) {
      toast({ title: 'Błąd', description: e instanceof Error ? e.message : 'Błąd', variant: 'destructive' });
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (authLoading) return;

    if (!user) { router.push('/login'); return; }
    load();
  }, [user, authLoading, router]);

  const handleCancel = async (id: string) => {
    try {
      await requestsApi.cancel(id);
      toast({ title: 'Wniosek anulowany' });
      load();
    } catch (e: unknown) {
      toast({ title: 'Błąd', description: e instanceof Error ? e.message : 'Błąd', variant: 'destructive' });
    }
  };

  return (
    <div className="min-h-screen bg-gray-50 p-8">
      <div className="max-w-4xl mx-auto">
        <div className="flex items-center gap-4 mb-6">
          <Link href="/"><Button variant="ghost" size="sm"><ArrowLeft className="h-4 w-4 mr-1" />Powrót</Button></Link>
          <div>
            <h1 className="text-2xl font-bold">Moje wnioski</h1>
            <p className="text-gray-600 text-sm">Historia rezerwacji zasobów</p>
          </div>
        </div>

        {loading ? (
          <p className="text-gray-500">Ładowanie...</p>
        ) : requests.length === 0 ? (
          <Card>
            <CardContent className="p-8 text-center text-gray-500">
              Brak wniosków. <Link href="/zasoby" className="text-blue-600 hover:underline">Złóż pierwszy wniosek</Link>.
            </CardContent>
          </Card>
        ) : (
          <div className="space-y-3">
            {requests.map(r => {
              const resource = resourceMap[r.resourceId];
              return (
                <Card key={r.id} className="hover:shadow-sm transition-shadow">
                  <CardContent className="p-4">
                    <div className="flex items-start justify-between gap-4">
                      <div className="flex-1 min-w-0">
                        <div className="flex items-center gap-2 mb-2 flex-wrap">
                          <span className={`text-xs px-2 py-0.5 rounded-full font-medium ${STATUS_LABELS[r.status]?.color}`}>
                            {STATUS_LABELS[r.status]?.label}
                          </span>
                          <span className="text-xs text-gray-400">
                            złożony {new Date(r.submittedAt).toLocaleDateString('pl-PL')}
                          </span>
                        </div>

                        <div className="flex items-center gap-1.5 mb-1">
                          <Box className="h-4 w-4 text-gray-500 flex-shrink-0" />
                          <span className="font-semibold text-sm">
                            {resource ? resource.name : `Zasób #${r.resourceId.slice(0, 8)}`}
                          </span>
                          {resource && (
                            <span className="text-xs bg-blue-50 text-blue-700 px-1.5 py-0.5 rounded">
                              {TYPE_LABELS[resource.type] || resource.type}
                            </span>
                          )}
                        </div>

                        {resource && (
                          <div className="flex items-center gap-1 text-xs text-gray-500 mb-1">
                            <MapPin className="h-3 w-3" />
                            {resource.building}, piętro {resource.floor}, nr {resource.number}
                          </div>
                        )}

                        <div className="flex items-center gap-1 text-sm text-gray-700">
                          <Calendar className="h-3.5 w-3.5 text-gray-400" />
                          {r.dateFrom} — {r.dateTo}
                        </div>

                        {r.justification && (
                          <p className="text-xs text-gray-500 mt-1 italic">„{r.justification}"</p>
                        )}
                        {r.rejectionReason && (
                          <p className="text-xs text-red-600 mt-1">Powód odrzucenia: {r.rejectionReason}</p>
                        )}
                      </div>

                      {(r.status === 'SUBMITTED' || r.status === 'APPROVED') && (
                        <Button
                          variant="outline"
                          size="sm"
                          className="text-red-600 border-red-200 hover:bg-red-50 flex-shrink-0"
                          onClick={() => handleCancel(r.id)}
                        >
                          <XCircle className="h-4 w-4 mr-1" />
                          Anuluj
                        </Button>
                      )}
                    </div>
                  </CardContent>
                </Card>
              );
            })}
          </div>
        )}
      </div>
    </div>
  );
}
