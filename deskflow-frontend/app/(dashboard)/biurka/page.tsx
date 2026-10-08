'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import Link from 'next/link';
import { ArrowLeft, Plus, MapPin, CheckCircle, XCircle } from 'lucide-react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogTrigger } from '@/components/ui/dialog';
import { useAuth } from '@/lib/auth-context';
import { resourcesApi, requestsApi, Resource } from '@/lib/api';
import { useToast } from '@/hooks/use-toast';

const TYPE_LABELS: Record<string, string> = {
  DESK: 'Biurko', CONFERENCE_ROOM: 'Sala', LAPTOP: 'Laptop',
  MONITOR: 'Monitor', PROJECTOR: 'Projektor', VEHICLE: 'Pojazd', ACCESSORY: 'Akcesoria',
};

const today = new Date().toISOString().split('T')[0];

function nextDay(date: string) {
  const match = /^(\d{4})-(\d{2})-(\d{2})$/.exec(date);
  if (!match) return today;

  const [, year, month, day] = match.map(Number);
  const value = new Date(Date.UTC(year, month - 1, day));
  if (
    Number.isNaN(value.getTime()) ||
    value.getUTCFullYear() !== year ||
    value.getUTCMonth() !== month - 1 ||
    value.getUTCDate() !== day
  ) return today;

  value.setUTCDate(value.getUTCDate() + 1);
  return value.toISOString().split('T')[0];
}

export default function ResourcesPage() {
  const { user, loading: authLoading } = useAuth();
  const router = useRouter();
  const { toast } = useToast();
  const [resources, setResources] = useState<Resource[]>([]);
  const [loading, setLoading] = useState(true);
  const [open, setOpen] = useState(false);
  const [selected, setSelected] = useState<Resource | null>(null);
  const [form, setForm] = useState({ dateFrom: '', dateTo: '', justification: '' });

  const load = () => resourcesApi.list().then(setResources).catch(() => {}).finally(() => setLoading(false));

  useEffect(() => {
    if (authLoading) return;

    if (!user) { router.push('/login'); return; }
    load();
  }, [user, authLoading, router]);

  const handleSubmit = async () => {
    if (!selected) return;

    if (form.dateFrom < today) {
      toast({ title: 'Błąd', description: 'Data od nie może być wcześniejsza niż dzisiaj.', variant: 'destructive' });
      return;
    }

    if (form.dateTo <= form.dateFrom) {
      toast({ title: 'Błąd', description: 'Data do musi być późniejsza niż Data od.', variant: 'destructive' });
      return;
    }

    try {
      await requestsApi.submit({ resourceId: selected.id, ...form });
      toast({ title: 'Wniosek złożony pomyślnie' });
      setOpen(false);
      setForm({ dateFrom: '', dateTo: '', justification: '' });
    } catch (e: unknown) {
      toast({ title: 'Błąd', description: e instanceof Error ? e.message : 'Błąd', variant: 'destructive' });
    }
  };

  return (
    <div className="min-h-screen bg-gray-50 p-8">
      <div className="max-w-6xl mx-auto">
        <div className="flex items-center gap-4 mb-6">
          <Link href="/"><Button variant="ghost" size="sm"><ArrowLeft className="h-4 w-4 mr-1" />Powrót</Button></Link>
          <div>
            <h1 className="text-2xl font-bold">Oferta zasobów</h1>
          </div>
        </div>

        {loading ? (
          <p className="text-gray-500">Ładowanie zasobów...</p>
        ) : (
          <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
            {resources.map(r => (
              <Card key={r.id} className={`${!r.available ? 'opacity-60' : 'hover:shadow-md transition-shadow'}`}>
                <CardHeader className="pb-2">
                  <div className="flex items-start justify-between">
                    <CardTitle className="text-base">{r.name}</CardTitle>
                    {r.available ? (
                      <span className="flex items-center gap-1 text-xs text-green-600">
                        <CheckCircle className="h-3 w-3" />Dostępny
                      </span>
                    ) : (
                      <span className="flex items-center gap-1 text-xs text-red-500">
                        <XCircle className="h-3 w-3" />Niedostępny
                      </span>
                    )}
                  </div>
                </CardHeader>
                <CardContent>
                  <div className="space-y-1 mb-3">
                    <span className="text-xs bg-blue-100 text-blue-700 px-2 py-0.5 rounded-full">
                      {TYPE_LABELS[r.type] || r.type}
                    </span>
                    <p className="text-sm text-gray-600 flex items-center gap-1 mt-2">
                      <MapPin className="h-3 w-3" />
                      {r.building}, piętro {r.floor}, nr {r.number}
                    </p>
                  </div>
                  <Dialog open={open && selected?.id === r.id} onOpenChange={v => { setOpen(v); if (!v) setSelected(null); }}>
                    <DialogTrigger asChild>
                      <Button
                        size="sm" className="w-full gap-2"
                        disabled={!r.available}
                        onClick={() => { setSelected(r); setOpen(true); }}
                      >
                        <Plus className="h-4 w-4" />Złóż wniosek
                      </Button>
                    </DialogTrigger>
                    <DialogContent>
                      <DialogHeader>
                        <DialogTitle>Złóż wniosek: {r.name}</DialogTitle>
                      </DialogHeader>
                      <div className="space-y-4 pt-2">
                        <div className="grid grid-cols-2 gap-3">
                          <div>
                            <Label>Data od</Label>
                            <Input
                              type="date"
                              min={today}
                              value={form.dateFrom}
                              onChange={e => setForm(f => ({ ...f, dateFrom: e.target.value }))}
                            />
                          </div>
                          <div>
                            <Label>Data do</Label>
                            <Input
                              type="date"
                              min={form.dateFrom ? nextDay(form.dateFrom) : today}
                              value={form.dateTo}
                              onChange={e => setForm(f => ({ ...f, dateTo: e.target.value }))}
                            />
                          </div>
                        </div>
                        <div>
                          <Label>Uzasadnienie</Label>
                          <Input
                            value={form.justification}
                            onChange={e => setForm(f => ({ ...f, justification: e.target.value }))}
                            placeholder="Cel wypożyczenia..."
                          />
                        </div>
                        <Button className="w-full" onClick={handleSubmit} disabled={!form.dateFrom || !form.dateTo}>
                          Złóż wniosek
                        </Button>
                      </div>
                    </DialogContent>
                  </Dialog>
                </CardContent>
              </Card>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
