'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import Link from 'next/link';
import { ArrowLeft, Check, X, Package, RotateCcw, Box, Calendar, MapPin, User } from 'lucide-react';
import { Card, CardContent } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Dialog, DialogContent, DialogHeader, DialogTitle } from '@/components/ui/dialog';
import { useAuth } from '@/lib/auth-context';
import { administrationApi, resourcesApi, AllocationRequest, Resource } from '@/lib/api';
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

export default function AdminPanelPage() {
  const { user, loading: authLoading, isManager } = useAuth();
  const router = useRouter();
  const { toast } = useToast();
  const [requests, setRequests] = useState<AllocationRequest[]>([]);
  const [resourceMap, setResourceMap] = useState<Record<string, Resource>>({});
  const [filterStatus, setFilterStatus] = useState('');
  const [loading, setLoading] = useState(true);
  const [rejectDialog, setRejectDialog] = useState<string | null>(null);
  const [reason, setReason] = useState('');

  const load = async () => {
    try {
      const [reqs, resources] = await Promise.all([
        administrationApi.requests(filterStatus || undefined),
        resourcesApi.list(),
      ]);
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
    if (!isManager()) { router.push('/'); return; }
    load();
  }, [user, authLoading, filterStatus, router]);

  const action = async (fn: () => Promise<unknown>, label: string) => {
    try {
      await fn();
      toast({ title: `${label} — sukces` });
      load();
    } catch (e: unknown) {
      toast({ title: 'Błąd', description: e instanceof Error ? e.message : 'Błąd', variant: 'destructive' });
    }
  };

  const handleReject = async () => {
    if (!rejectDialog) return;
    await action(() => administrationApi.reject(rejectDialog, reason), 'Wniosek odrzucony');
    setRejectDialog(null);
    setReason('');
  };

  return (
    <div className="min-h-screen bg-gray-50 p-8">
      <div className="max-w-5xl mx-auto">
        <div className="flex items-center gap-4 mb-6">
          <Link href="/"><Button variant="ghost" size="sm"><ArrowLeft className="h-4 w-4 mr-1" />Powrót</Button></Link>
          <div>
            <h1 className="text-2xl font-bold">Panel administracji</h1>
            <p className="text-gray-600 text-sm">Zarządzanie wnioskami o zasoby</p>
          </div>
        </div>

        <div className="flex gap-2 mb-6 flex-wrap">
          {['', 'SUBMITTED', 'APPROVED', 'ACTIVE', 'COMPLETED', 'OVERDUE'].map(s => (
            <Button
              key={s}
              variant={filterStatus === s ? 'default' : 'outline'}
              size="sm"
              onClick={() => setFilterStatus(s)}
            >
              {s === '' ? 'Wszystkie' : STATUS_LABELS[s]?.label}
            </Button>
          ))}
        </div>

        {loading ? (
          <p className="text-gray-500">Ładowanie...</p>
        ) : requests.length === 0 ? (
          <Card><CardContent className="p-8 text-center text-gray-500">Brak wniosków.</CardContent></Card>
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
                          <User className="h-3.5 w-3.5 text-gray-400 flex-shrink-0" />
                          <span className="text-sm text-gray-700">
                            {r.employeeFirstName && r.employeeLastName
                              ? <span className="font-medium">{r.employeeFirstName} {r.employeeLastName}</span>
                              : <span className="font-mono text-xs text-gray-400">{r.employeeId}</span>
                            }
                          </span>
                        </div>

                        <div className="flex items-center gap-1.5 mb-1">
                          <Box className="h-3.5 w-3.5 text-gray-500 flex-shrink-0" />
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

                        {r.justification && <p className="text-xs text-gray-500 mt-1 italic">„{r.justification}"</p>}
                        {r.rejectionReason && <p className="text-xs text-red-500 mt-1">Powód: {r.rejectionReason}</p>}
                      </div>

                      <div className="flex gap-2 flex-shrink-0 flex-wrap justify-end">
                        {r.status === 'SUBMITTED' && (
                          <>
                            <Button size="sm" className="gap-1 bg-green-600 hover:bg-green-700"
                              onClick={() => action(() => administrationApi.approve(r.id), 'Zatwierdzono')}>
                              <Check className="h-3 w-3" />Zatwierdź
                            </Button>
                            <Button size="sm" variant="destructive" className="gap-1"
                              onClick={() => setRejectDialog(r.id)}>
                              <X className="h-3 w-3" />Odrzuć
                            </Button>
                          </>
                        )}
                        {r.status === 'APPROVED' && (
                          <Button size="sm" className="gap-1"
                            onClick={() => action(() => administrationApi.issue(r.id), 'Zasób wydany')}>
                            <Package className="h-3 w-3" />Wydaj zasób
                          </Button>
                        )}
                        {(r.status === 'ACTIVE' || r.status === 'OVERDUE') && (
                          <Button size="sm" variant="outline" className="gap-1"
                            onClick={() => action(() => administrationApi.return(r.id), 'Zasób zwrócony')}>
                            <RotateCcw className="h-3 w-3" />Zwróć zasób
                          </Button>
                        )}
                      </div>
                    </div>
                  </CardContent>
                </Card>
              );
            })}
          </div>
        )}
      </div>

      <Dialog open={!!rejectDialog} onOpenChange={v => !v && setRejectDialog(null)}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Odrzuć wniosek</DialogTitle>
          </DialogHeader>
          <div className="space-y-4">
            <Input
              placeholder="Podaj powód odrzucenia..."
              value={reason}
              onChange={e => setReason(e.target.value)}
            />
            <div className="flex gap-2 justify-end">
              <Button variant="outline" onClick={() => setRejectDialog(null)}>Anuluj</Button>
              <Button variant="destructive" onClick={handleReject} disabled={!reason.trim()}>
                Odrzuć
              </Button>
            </div>
          </div>
        </DialogContent>
      </Dialog>
    </div>
  );
}
