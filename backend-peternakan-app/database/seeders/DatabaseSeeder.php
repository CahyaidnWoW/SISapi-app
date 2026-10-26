<?php

namespace Database\Seeders;

use App\Models\Cage;
use App\Models\FeedStock;
use App\Models\FeedStockBatch;
use App\Models\FeedConsumption;
use App\Models\HealthRecord;
use App\Models\Livestock;
use App\Models\LivestockBreed;
use App\Models\User;
use App\Models\WeightLog;
use Carbon\Carbon;
use Illuminate\Database\Seeder;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Hash;

class DatabaseSeeder extends Seeder
{
    private const DEMO_PASSWORD = 'SiSapiDemo123!';

    public function run(): void
    {
        DB::transaction(function () {
            [$manager, $vet, $worker] = $this->seedUsers();
            $cages = $this->seedCages();
            $breeds = $this->seedBreeds();
            $this->seedLivestock($cages, $breeds);
            $this->refreshCagePopulation($cages);
            $feedStocks = $this->seedFeedStock();
            $this->seedFeedConsumptions($cages, $feedStocks, $worker);
            $this->seedWeightLogs($worker);
            $this->seedHealthRecords($vet, $worker);
        });

        $this->command->info('DatabaseSeeder selesai: 20 sapi realistis dan seluruh riwayat data siap digunakan.');
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
            $user = User::firstOrCreate(
                ['email' => $email],
                [
                    'name'     => $name,
                    'password' => Hash::make(self::DEMO_PASSWORD),
                    'role'     => $role,
                    'phone'    => $phone,
                ]
            );
            $users[$key] = $user;
        }

        return [$users['manager'], $users['vet'], $users['worker']];
    }

    private function seedCages(): array
    {
        $definitions = [
            'A' => ['Kandang A (Penggemukan 1)', 15, 'Blok Utara'],
            'B' => ['Kandang B (Penggemukan 2)', 15, 'Blok Timur'],
            'C' => ['Kandang C (Adaptasi & Isolasi)', 10, 'Blok Selatan'],
        ];

        $cages = [];
        foreach ($definitions as $letter => [$name, $capacity, $location]) {
            $cages[$letter] = Cage::firstOrCreate(
                ['name' => $name],
                [
                    'capacity'           => $capacity,
                    'current_population' => 0,
                    'location'           => $location,
                ]
            );
        }

        return $cages;
    }

    private function seedBreeds(): array
    {
        $definitions = [
            'Simental' => '5.5 - 7.0',
            'Limousin' => '5.5 - 7.0',
            'Brahman'  => '6.0 - 8.0',
            'Angus'    => '5.0 - 6.5',
        ];

        $breeds = [];
        foreach ($definitions as $name => $range) {
            $breeds[$name] = LivestockBreed::firstOrCreate(
                ['breed_name' => $name],
                ['ideal_fcr_range' => $range]
            );
        }

        return $breeds;
    }

    private function seedLivestock(array $cages, array $breeds): void
    {
        $cows = [
            ['SP-001', 'A', 'Simental', 'male',   '2024-03-01', '2024-10-01', 320.0, 'healthy'],
            ['SP-002', 'A', 'Simental', 'male',   '2024-03-15', '2024-10-01', 310.0, 'healthy'],
            ['SP-003', 'A', 'Limousin', 'male',   '2024-02-20', '2024-10-01', 345.0, 'healthy'],
            ['SP-004', 'A', 'Brahman',  'male',   '2024-04-10', '2024-11-01', 290.0, 'healthy'],
            ['SP-005', 'A', 'Angus',    'male',   '2024-04-25', '2024-11-01', 305.0, 'healthy'],
            ['SP-006', 'A', 'Simental', 'male',   '2024-03-10', '2024-11-01', 330.0, 'healthy'],
            ['SP-007', 'A', 'Limousin', 'male',   '2024-05-01', '2024-11-15', 280.0, 'healthy'],

            ['SP-008', 'B', 'Simental', 'male',   '2024-02-15', '2024-10-15', 335.0, 'healthy'],
            ['SP-009', 'B', 'Brahman',  'female', '2024-05-10', '2024-11-15', 275.0, 'healthy'],
            ['SP-010', 'B', 'Limousin', 'male',   '2024-01-20', '2024-09-01', 360.0, 'healthy'],
            ['SP-011', 'B', 'Angus',    'male',   '2024-04-15', '2024-12-01', 295.0, 'healthy'],
            ['SP-012', 'B', 'Simental', 'female', '2024-06-01', '2024-12-01', 285.0, 'sick'],
            ['SP-013', 'B', 'Limousin', 'male',   '2024-03-20', '2024-12-01', 315.0, 'healthy'],
            ['SP-014', 'B', 'Brahman',  'male',   '2024-05-15', '2024-12-15', 270.0, 'healthy'],

            ['SP-015', 'C', 'Simental', 'male',   '2024-01-10', '2024-08-15', 350.0, 'healthy'],
            ['SP-016', 'C', 'Limousin', 'male',   '2024-04-05', '2025-01-05', 290.0, 'healthy'],
            ['SP-017', 'C', 'Brahman',  'female', '2024-06-20', '2025-01-05', 265.0, 'sick'],
            ['SP-018', 'C', 'Angus',    'male',   '2024-05-10', '2025-01-15', 300.0, 'healthy'],
            ['SP-019', 'C', 'Simental', 'male',   '2024-03-25', '2025-01-15', 310.0, 'healthy'],
            ['SP-020', 'C', 'Limousin', 'male',   '2024-02-01', '2024-08-01', 340.0, 'sold'],
        ];

        foreach ($cows as [$tag, $cageKey, $breedKey, $gender, $birth, $entry, $weight, $status]) {
            Livestock::firstOrCreate(
                ['tag_number' => $tag],
                [
                    'cage_id'        => $cages[$cageKey]->id,
                    'breed_id'       => $breeds[$breedKey]->id,
                    'gender'         => $gender,
                    'birth_date'     => $birth,
                    'entry_date'     => $entry,
                    'initial_weight' => $weight,
                    'status'         => $status,
                ]
            );
        }
    }

    private function refreshCagePopulation(array $cages): void
    {
        foreach ($cages as $cage) {
            $cage->update([
                'current_population' => Livestock::where('cage_id', $cage->id)
                    ->whereIn('status', ['healthy', 'sick'])
                    ->count(),
            ]);
        }
    }

    private function seedFeedStock(): array
    {
        $feeds = [
            ['Jerami Fermentasi', 'kg', [[1200, 30], [800, 10]]],
            ['Konsentrat Penggemukan', 'kg', [[1500, 25], [1000, 5]]],
            ['Silase Jagung', 'kg', [[2000, 20]]],
            ['Mineral & Suplemen', 'kg', [[45, 15]]], 
        ];

        $results = [];
        foreach ($feeds as [$name, $unit, $batches]) {
            $feed = FeedStock::firstOrCreate(
                ['feed_name' => $name],
                ['unit' => $unit]
            );

            if (FeedStockBatch::where('feed_id', $feed->id)->count() === 0) {
                foreach ($batches as [$qty, $daysAgo]) {
                    FeedStockBatch::create([
                        'feed_id'            => $feed->id,
                        'quantity_in'        => $qty,
                        'quantity_remaining' => $qty * 0.7,
                        'entry_date'         => Carbon::now()->subDays($daysAgo)->toDateString(),
                    ]);
                }
            }
            $results[$name] = $feed;
        }

        return $results;
    }

        private function seedFeedConsumptions(array $cages, array $feeds, User $worker): void
    {
        if (FeedConsumption::count() > 0) return;

        $konsentrat = $feeds['Konsentrat Penggemukan']->id;
        $silase = $feeds['Silase Jagung']->id;

        for ($i = 14; $i >= 1; $i--) {
            $date = Carbon::now()->subDays($i)->toDateString();
            foreach ($cages as $cage) {
                FeedConsumption::create([
                    'cage_id'     => $cage->id,
                    'feed_id'     => $konsentrat,
                    'amount_used' => rand(40, 65),
                    'date'        => $date,
                    'worker_id'   => $worker->id,
                ]);
                FeedConsumption::create([
                    'cage_id'     => $cage->id,
                    'feed_id'     => $silase,
                    'amount_used' => rand(70, 110),
                    'date'        => $date,
                    'worker_id'   => $worker->id,
                ]);
            }
        }
    }


    private function seedWeightLogs(User $worker): void
    {
        $logs = [
            'SP-001' => [[355.0, 120], [390.0, 90], [425.0, 60], [455.0, 30], [472.0, 3]],
            'SP-002' => [[340.0, 120], [365.0, 90], [395.0, 60], [420.0, 30], [438.0, 3]],
            'SP-003' => [[380.0, 120], [410.0, 90], [440.0, 60], [465.0, 30], [485.0, 2]],
            'SP-004' => [[315.0, 90], [345.0, 60], [375.0, 30], [398.0, 3]],
            'SP-005' => [[330.0, 90], [360.0, 60], [388.0, 30], [412.0, 3]],
            'SP-006' => [[355.0, 90], [385.0, 60], [410.0, 30], [428.0, 4]],
            'SP-007' => [[305.0, 75], [335.0, 45], [362.0, 15], [378.0, 2]],

            'SP-008' => [[365.0, 105], [395.0, 75], [425.0, 45], [445.0, 15], [458.0, 2]], 
            'SP-009' => [[295.0, 75], [320.0, 45], [345.0, 15], [360.0, 2]],
            'SP-010' => [[395.0, 135], [425.0, 105], [455.0, 75], [480.0, 45], [510.0, 1]], 
            'SP-011' => [[320.0, 60], [350.0, 30], [374.0, 3]],
            'SP-012' => [[305.0, 60], [325.0, 30], [338.0, 3]],
            'SP-013' => [[340.0, 60], [370.0, 30], [395.0, 2]],
            'SP-014' => [[290.0, 45], [315.0, 20], [332.0, 2]],

            'SP-015' => [[385.0, 150], [415.0, 120], [445.0, 90], [470.0, 60], [498.0, 1]], 
            'SP-016' => [[310.0, 30], [332.0, 2]],
            'SP-017' => [[280.0, 30], [294.0, 2]],
            'SP-018' => [[320.0, 20], [338.0, 2]],
            'SP-019' => [[328.0, 20], [345.0, 2]],
            'SP-020' => [[370.0, 150], [405.0, 120], [440.0, 90], [465.0, 60]],
        ];

        foreach ($logs as $tag => $points) {
            $cow = Livestock::where('tag_number', $tag)->first();
            if (!$cow || WeightLog::where('livestock_id', $cow->id)->count() > 0) continue;

            foreach ($points as [$weight, $daysAgo]) {
                WeightLog::create([
                    'livestock_id' => $cow->id,
                    'weight'       => $weight,
                    'date'         => Carbon::now()->subDays($daysAgo)->toDateString(),
                    'worker_id'    => $worker->id,
                ]);
            }
        }
    }

    private function seedHealthRecords(User $vet, User $worker): void
    {
        if (HealthRecord::count() > 0) return;

        $records = [
            ['SP-001', 'vaccination', 'Vaksin PMK Dosis 2', 'Suntik 2 ml subkutan', 2, -28, $vet],
            ['SP-004', 'vaccination', 'Vaksin Antraks Tahunan', 'Suntik 1 ml intramuskular', 4, -360, $vet], 
            ['SP-007', 'vaccination', 'Vaksin Booster PMK', 'Suntik 2 ml subkutan', 6, -180, $vet],  
            ['SP-011', 'vaccination', 'Pemberian Obat Cacing & Vitamin', 'Oral albendazole 20 ml', 7, -90, $vet], 

            ['SP-012', 'treatment', 'Demam & Nafsu Makan Menurun', 'Injeksi Vitamin B-Kompleks & Antipiretik', null, -2, $vet],
            ['SP-017', 'emergency_report', 'Luka Lecet Kaki Kiri Belakang', 'Pembersihan luka iodine & perban', null, -1, $worker],

            ['SP-002', 'vaccination', 'Vaksin PMK Dosis 1', 'Suntik 2 ml subkutan', null, -60, $vet],
            ['SP-003', 'vaccination', 'Vaksin PMK Dosis 1', 'Suntik 2 ml subkutan', null, -60, $vet],
            ['SP-008', 'treatment', 'Pencegahan Kembung (Tympani)', 'Pemberian cairan antibloat 100 ml', null, -15, $vet],
        ];

        foreach ($records as [$tag, $type, $diagnosis, $treatment, $nextDue, $dateOffset, $handler]) {
            $cow = Livestock::where('tag_number', $tag)->first();
            if (!$cow) continue;

            HealthRecord::create([
                'livestock_id'  => $cow->id,
                'type'          => $type,
                'diagnosis'     => $diagnosis,
                'treatment'     => $treatment,
                'next_due_date' => $nextDue !== null ? Carbon::now()->addDays($nextDue)->toDateString() : null,
                'date'          => Carbon::now()->addDays($dateOffset)->toDateString(),
                'handled_by'    => $handler->id,
            ]);
        }
    }
}
