<?php

namespace Database\Seeders;

use App\Models\Cage;
use App\Models\FeedStock;
use App\Models\FeedStockBatch;
use App\Models\HealthRecord;
use App\Models\Livestock;
use App\Models\LivestockBreed;
use App\Models\User;
use App\Models\WeightLog;
use Carbon\Carbon;
use Illuminate\Database\Seeder;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Hash;

class DemoSeeder extends Seeder
{
    private const DEMO_PASSWORD = 'SiSapiDemo123!';
    private const RESET_DEMO_PASSWORDS = false;

    /** @var array<string,int> */
    private array $created = [];

    public function run(): void
    {
        DB::transaction(function () {
            [$manager, $vet, $worker] = $this->seedUsers();
            $cages = $this->seedCages();
            $breeds = $this->seedBreeds();
            $this->seedLivestock($cages, $breeds);
            $this->refreshCagePopulation($cages);
            $this->seedFeedStock();
            $this->seedWeightLogs($worker);
            $this->seedHealthRecords($vet, $worker);
        });

        $summary = collect($this->created)
            ->map(fn ($n, $k) => "$k: $n")
            ->implode(', ');

        $this->command->info('DemoSeeder selesai. Dibuat baru -> ' . ($summary !== '' ? $summary : 'tidak ada (semua data sudah ada)'));
    }


    private function count(string $key): void
    {
        $this->created[$key] = ($this->created[$key] ?? 0) + 1;
    }

    private function firstOrCreateForced(string $key, string $model, array $match, array $values = [])
    {
        $record = $model::where($match)->first();
        if ($record) {
            return $record;
        }

        $record = new $model();
        $record->forceFill($match + $values)->save();
        $this->count($key);

        return $record;
    }

    private function firstOrCreateByPrefix(string $key, string $model, string $column, string $prefix, string $createName, array $values = [])
    {
        $record = $model::where($column, 'like', $prefix . '%')->orderBy('id')->first();
        if ($record) {
            return $record;
        }

        $record = new $model();
        $record->forceFill([$column => $createName] + $values)->save();
        $this->count($key);

        return $record;
    }


    private function seedUsers(): array
    {
        $definitions = [
            'manager' => ['manager@farm.com', 'Budi Manager', 'manager', '081234567890'],
            'vet'     => ['vet@farm.com',     'drh. Sari',    'vet',     '081234567891'],
            'worker'  => ['worker@farm.com',  'Joko Pekerja', 'worker',  '081234567892'],
        ];

        $users = [];
        foreach ($definitions as $key => [$email, $name, $role, $phone]) {
            $user = User::where('email', $email)->first();

            if (!$user) {
                $user = new User();
                $user->forceFill([
                    'name' => $name,
                    'email' => $email,
                    'password' => Hash::make(self::DEMO_PASSWORD),
                    'role' => $role,
                    'phone' => $phone,
                ])->save();
                $this->count('user');
                $this->command->info("Akun dibuat: $email");
            } elseif (self::RESET_DEMO_PASSWORDS) {
                $user->forceFill(['password' => Hash::make(self::DEMO_PASSWORD)])->save();
                $this->command->warn("Akun sudah ada, password DIUBAH: $email");
            } else {
                $this->command->warn("Akun sudah ada, password TIDAK diubah: $email");
            }

            $users[$key] = $user;
        }

        return [$users['manager'], $users['vet'], $users['worker']];
    }

    private function seedCages(): array
    {
        $definitions = [
            'A' => ['Kandang A', 20, 'Blok Utara'],
            'B' => ['Kandang B', 20, 'Blok Timur'],
            'C' => ['Kandang C', 15, 'Blok Selatan'],
        ];

        $cages = [];
        foreach ($definitions as $letter => [$name, $capacity, $location]) {
            $cages[$letter] = $this->firstOrCreateByPrefix('kandang', Cage::class, 'name', $name, $name, [
                'capacity' => $capacity,
                'current_population' => 0,
                'location' => $location,
            ]);
        }

        return $cages;
    }

    private function seedBreeds(): array
    {
        $definitions = [
            'Simental' => '5.5 - 7.0',
            'Limousin' => '5.5 - 7.0',
            'Brahman'  => '6.0 - 8.0',
        ];

        $breeds = [];
        foreach ($definitions as $name => $range) {
            $breeds[$name] = $this->firstOrCreateForced('ras', LivestockBreed::class,
                ['breed_name' => $name],
                ['ideal_fcr_range' => $range]
            );
        }

        return $breeds;
    }

    private function seedLivestock(array $cages, array $breeds): void
    {
        $rows = [
            ['SP-001', 'A', 'Simental', 'male',   '2024-06-01', '2025-01-10', 320.0, 'healthy'],
            ['SP-002', 'A', 'Simental', 'female', '2024-05-15', '2025-01-10', 295.0, 'healthy'],
            ['SP-003', 'A', 'Limousin', 'male',   '2024-04-20', '2025-01-10', 340.0, 'healthy'],
            ['SP-004', 'A', 'Brahman',  'male',   '2024-07-01', '2025-02-01', 310.0, 'healthy'],
            ['SP-005', 'B', 'Simental', 'female', '2024-06-10', '2025-02-01', 285.0, 'healthy'],
            ['SP-006', 'B', 'Limousin', 'male',   '2024-05-05', '2025-02-01', 355.0, 'sick'],
            ['SP-007', 'B', 'Brahman',  'female', '2024-08-01', '2025-02-15', 270.0, 'healthy'],
            ['SP-008', 'C', 'Simental', 'male',   '2024-06-20', '2025-03-01', 330.0, 'healthy'],
            ['SP-009', 'C', 'Limousin', 'male',   '2024-07-15', '2025-03-01', 315.0, 'sick'],
            ['SP-010', 'C', 'Brahman',  'male',   '2024-04-10', '2025-03-15', 380.0, 'healthy'],
        ];

        foreach ($rows as [$tag, $cage, $breed, $gender, $birth, $entry, $weight, $status]) {
            $this->firstOrCreateForced('sapi', Livestock::class,
                ['tag_number' => $tag],
                [
                    'cage_id' => $cages[$cage]->id,
                    'breed_id' => $breeds[$breed]->id,
                    'gender' => $gender,
                    'birth_date' => $birth,
                    'entry_date' => $entry,
                    'initial_weight' => $weight,
                    'status' => $status,
                ]
            );
        }
    }

    private function refreshCagePopulation(array $cages): void
    {
        foreach ($cages as $cage) {
            $cage->forceFill([
                'current_population' => Livestock::where('cage_id', $cage->id)->count(),
            ])->save();
        }
    }

    private function seedFeedStock(): void
    {
        $feeds = [
            ['Jerami',     'Jerami Fermentasi', [[500, 20], [300, 5]]],
            ['Konsentrat', 'Konsentrat',        [[400, 10]]],
            ['Hijauan',    'Hijauan Segar',     [[600, 3]]],
        ];

        foreach ($feeds as [$prefix, $name, $batches]) {
            $feed = $this->firstOrCreateByPrefix('pakan', FeedStock::class, 'feed_name', $prefix, $name, ['unit' => 'kg']);

            $target = array_sum(array_column($batches, 0));
            $remaining = (float) FeedStockBatch::where('feed_id', $feed->id)->sum('quantity_remaining');

            if ($remaining < $target / 2) {
                foreach ($batches as [$qty, $daysAgo]) {
                    $batch = new FeedStockBatch();
                    $batch->forceFill([
                        'feed_id' => $feed->id,
                        'quantity_in' => $qty,
                        'quantity_remaining' => $qty,
                        'entry_date' => Carbon::now()->subDays($daysAgo)->toDateString(),
                    ])->save();
                    $this->count('batch pakan');
                }
            }
        }
    }

    private function seedWeightLogs(User $worker): void
    {
        $weights = [
            'SP-001' => [[340.0, 15], [348.5, 8], [356.0, 2]],
            'SP-002' => [[298.0, 14], [305.0, 8], [312.0, 2]],
            'SP-003' => [[348.0, 14], [355.0, 8], [363.0, 2]],
            'SP-004' => [[316.0, 13], [323.0, 7], [330.0, 1]],
            'SP-005' => [[291.0, 12], [298.0, 6], [304.0, 1]],
            'SP-006' => [[360.0, 12], [366.0, 6], [370.0, 1]],
            'SP-007' => [[276.0, 12], [281.0, 6], [287.0, 1]],
            'SP-008' => [[336.0, 10], [343.0, 5], [350.0, 1]],
            'SP-009' => [[320.0, 10], [325.0, 5], [329.0, 1]],
            'SP-010' => [[388.0, 9], [394.0, 4], [400.0, 1]],
        ];

        foreach ($weights as $tag => $points) {
            $cow = Livestock::where('tag_number', $tag)->first();
            if (!$cow || WeightLog::where('livestock_id', $cow->id)->exists()) {
                continue;
            }

            foreach ($points as [$weight, $daysAgo]) {
                $log = new WeightLog();
                $log->forceFill([
                    'livestock_id' => $cow->id,
                    'weight' => $weight,
                    'date' => Carbon::now()->subDays($daysAgo)->toDateString(),
                    'worker_id' => $worker->id,
                ])->save();
                $this->count('timbangan');
            }
        }
    }

    private function seedHealthRecords(User $vet, User $worker): void
    {
        $rows = [
            ['SP-001', 'vaccination',      'Vaksin PMK dosis 1',          'Suntik 2 ml subkutan',              14,   -14, $vet],
            ['SP-001', 'vaccination',      'Vaksin PMK dosis 2',          'Suntik 2 ml subkutan',               3,    -2, $vet],
            ['SP-002', 'vaccination',      'Vaksin PMK dosis 1',          'Suntik 2 ml subkutan',              20,   -10, $vet],
            ['SP-003', 'vaccination',      'Vaksin Antraks',              'Suntik 1 ml intramuskular',         -2,   -20, $vet],
            ['SP-004', 'vaccination',      'Vaksin PMK dosis 1',          'Suntik 2 ml subkutan',               5,    -7, $vet],
            ['SP-005', 'vaccination',      'Vaksin Antraks',              'Suntik 1 ml intramuskular',          1,   -29, $vet],
            ['SP-008', 'treatment',        'Cacingan ringan',             'Obat cacing oral 10 ml',          null,    -3, $vet],
            ['SP-006', 'emergency_report', 'Pincang kaki kanan belakang', 'Sapi dipisah ke kandang isolasi', null,    -1, $worker],
            ['SP-009', 'emergency_report', 'Nafsu makan turun sejak pagi', 'Diberi vitamin B kompleks',      null,    -2, $worker],
        ];

        foreach ($rows as [$tag, $type, $diagnosis, $treatment, $nextDue, $dateOffset, $handler]) {
            $cow = Livestock::where('tag_number', $tag)->first();
            if (!$cow) {
                continue;
            }

            $exists = HealthRecord::where('livestock_id', $cow->id)
                ->where('type', $type)
                ->where('diagnosis', $diagnosis)
                ->exists();
            if ($exists) {
                continue;
            }

            $record = new HealthRecord();
            $record->forceFill([
                'livestock_id' => $cow->id,
                'type' => $type,
                'diagnosis' => $diagnosis,
                'treatment' => $treatment,
                'next_due_date' => $nextDue !== null ? Carbon::now()->addDays($nextDue)->toDateString() : null,
                'date' => Carbon::now()->addDays($dateOffset)->toDateString(),
                'handled_by' => $handler->id,
            ])->save();
            $this->count('rekam medis');
        }
    }
}