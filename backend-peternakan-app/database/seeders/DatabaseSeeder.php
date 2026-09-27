<?php

namespace Database\Seeders;

use App\Models\User;
use App\Models\Cage;
use App\Models\LivestockBreed;
use App\Models\FeedStock;
use App\Models\FeedStockBatch;
use App\Models\Livestock;
use App\Models\WeightLog;
use Illuminate\Database\Seeder;
use Illuminate\Support\Facades\Hash;

class DatabaseSeeder extends Seeder
{
    public function run(): void
    {
        // 1. Seed Users (3 Role Utama)
        $manager = User::create([
            'name' => 'Budi Manager',
            'email' => 'manager@farm.com',
            'password' => Hash::make('password'),
            'role' => 'manager',
            'phone' => '081234567890',
        ]);

        $vet = User::create([
            'name' => 'drh. Siti Vet',
            'email' => 'vet@farm.com',
            'password' => Hash::make('password'),
            'role' => 'vet',
            'phone' => '081234567891',
        ]);

        $worker = User::create([
            'name' => 'Joko Pekerja',
            'email' => 'worker@farm.com',
            'password' => Hash::make('password'),
            'role' => 'worker',
            'phone' => '081234567892',
        ]);

        // 2. Seed Ras Sapi
        $simental = LivestockBreed::create(['breed_name' => 'Simental', 'ideal_fcr_range' => '5.5 - 6.5']);
        $limousin = LivestockBreed::create(['breed_name' => 'Limousin', 'ideal_fcr_range' => '5.0 - 6.0']);
        $brahman  = LivestockBreed::create(['breed_name' => 'Brahman', 'ideal_fcr_range' => '6.0 - 7.0']);

        // 3. Seed Kandang
        $kandangA = Cage::create(['name' => 'Kandang A (Gemuk)', 'capacity' => 10, 'current_population' => 2, 'location' => 'Blok Timur']);
        $kandangB = Cage::create(['name' => 'Kandang B (Isolasi)', 'capacity' => 5, 'current_population' => 0, 'location' => 'Blok Barat']);

        // 4. Seed Pakan & Batch FIFO
        $konsentrat = FeedStock::create(['feed_name' => 'Konsentrat Sapi', 'unit' => 'kg']);
        $jerami     = FeedStock::create(['feed_name' => 'Jerami Kering', 'unit' => 'kg']);

        FeedStockBatch::create([
            'feed_id' => $konsentrat->id,
            'quantity_in' => 500,
            'quantity_remaining' => 500,
            'entry_date' => now()->subDays(5),
        ]);

        // 5. Seed Sapi Dummy
        $sapi1 = Livestock::create([
            'tag_number' => 'SP-001',
            'cage_id' => $kandangA->id,
            'breed_id' => $simental->id,
            'gender' => 'male',
            'birth_date' => now()->subMonths(18),
            'entry_date' => now()->subMonths(2),
            'initial_weight' => 300.00,
            'status' => 'healthy',
        ]);

        $sapi2 = Livestock::create([
            'tag_number' => 'SP-002',
            'cage_id' => $kandangA->id,
            'breed_id' => $limousin->id,
            'gender' => 'male',
            'birth_date' => now()->subMonths(20),
            'entry_date' => now()->subMonths(2),
            'initial_weight' => 320.00,
            'status' => 'healthy',
        ]);

        // 6. Seed Histori Timbangan Awal
        WeightLog::create([
            'livestock_id' => $sapi1->id,
            'weight' => 300.00,
            'date' => now()->subMonths(2),
            'worker_id' => $worker->id,
        ]);

        WeightLog::create([
            'livestock_id' => $sapi1->id,
            'weight' => 335.50,
            'date' => now()->subMonths(1),
            'worker_id' => $worker->id,
        ]);
    }
}
