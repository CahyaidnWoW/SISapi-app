<!DOCTYPE html>
<html lang="id">
<head>
    <meta charset="UTF-8">
    <title>Laporan Operasional Peternakan</title>
    @include('reports.partials.styles')
</head>
<body>

    <table class="kop-table">
        <tr>
            <td class="kop-logo-cell">
                @if(file_exists(public_path('images/logo_perusahaan.png')))
                    <img src="{{ public_path('images/logo_perusahaan.png') }}" class="kop-logo-img">
                @else
                    <div class="kop-logo-placeholder">LOGO KIRI</div>
                @endif
            </td>
            <td class="kop-text-cell">
                <div class="kop-instansi">Sistem Manajemen Peternakan Sapi Potong</div>
                <div class="kop-nama-farm">SI-SAPI INTEGRATED FARM</div>
                <div class="kop-alamat">
                    Kawasan Agribisnis Terpadu | Jl. Raya Peternakan No. 01, Sumedang<br>
                    Telp: (0261) 201234 | Email: admin@sisapi.sch.id | Website: www.sisapi.sch.id
                </div>
            </td>
            <td class="kop-logo-cell">
                @if(file_exists(public_path('images/logo_sisapi.png')))
                    <img src="{{ public_path('images/logo_sisapi.png') }}" class="kop-logo-img">
                @else
                    <div class="kop-logo-placeholder">LOGO KANAN</div>
                @endif
            </td>
        </tr>
    </table>

    <div class="report-title">
        <h3>Laporan Operasional &amp; Perkembangan Ternak ({{ strtoupper($periodName) }})</h3>
        <p>Periode: <strong>{{ $startDate }} s/d {{ $endDate }}</strong> | Dicetak oleh: {{ $generatedBy }} ({{ now()->format('d/m/Y H:i') }})</p>
    </div>
    <table class="kpi-table">
        <tr>
            <td class="kpi-box">
                <div class="kpi-label">Total Sapi Aktif</div>
                <div class="kpi-val">{{ $summary['total_cattle'] }} Ekor</div>
            </td>
            <td class="kpi-box">
                <div class="kpi-label">Siap Panen (>=450kg)</div>
                <div class="kpi-val">{{ $summary['ready_to_sell'] }} Ekor</div>
            </td>
            <td class="kpi-box">
                <div class="kpi-label">Sapi Sakit / Karantina</div>
                <div class="kpi-val">{{ $summary['sick_count'] }} Ekor</div>
            </td>
            <td class="kpi-box">
                <div class="kpi-label">Konsumsi Pakan</div>
                <div class="kpi-val">{{ number_format($summary['total_feed_consumed'], 1) }} kg</div>
            </td>
        </tr>
    </table>

    <div class="section-header">1. Populasi Ternak &amp; Penimbangan Bobot Terakhir</div>
    <table class="data-table">
        <thead>
            <tr>
                <th width="5%" class="text-center">No</th>
                <th width="15%">Tag Sapi</th>
                <th width="22%">Kandang</th>
                <th width="16%">Ras</th>
                <th width="14%" class="text-right">Bobot Masuk</th>
                <th width="14%" class="text-right">Bobot Terkini</th>
                <th width="14%" class="text-center">Status</th>
            </tr>
        </thead>

        <tbody>
            @forelse($livestocks as $idx => $item)
            <tr>
                <td class="text-center">{{ $idx + 1 }}</td>
                <td><strong>{{ $item->tag_number }}</strong></td>
                <td>{{ $item->cage->name ?? '-' }}</td>
                <td>{{ $item->breed->breed_name ?? '-' }}</td>
                <td class="text-right">{{ number_format($item->initial_weight, 1) }} kg</td>
                <td class="text-right">{{ number_format($item->latest_weight, 1) }} kg</td>
                <td class="text-center">{{ ucfirst($item->status) }}</td>
            </tr>
            @empty
            <tr>
                <td colspan="7" class="text-center">Tidak ada data ternak</td>
            </tr>
            @endforelse
        </tbody>
    </table>

    <div class="section-header">2. Rekap Konsumsi Pakan Periode Ini</div>
    <table class="data-table">
                <thead>
            <tr>
                <th width="5%" class="text-center">No</th>
                <th width="28%">Nama Kandang</th>
                <th width="28%">Jenis Pakan</th>
                <th width="21%" class="text-right">Jumlah Terpakai</th>
                <th width="18%" class="text-center">Tanggal Pemakaian</th>
            </tr>
        </thead>

        <tbody>
            @forelse($feedConsumptions as $idx => $fc)
            <tr>
                <td class="text-center">{{ $idx + 1 }}</td>
                <td>{{ $fc->cage->name ?? '-' }}</td>
                <td>{{ $fc->feedStock->feed_name ?? '-' }}</td>
                <td class="text-right">{{ number_format($fc->amount_used, 1) }} {{ $fc->feedStock->unit ?? 'kg' }}</td>
                <td class="text-center">{{ \Carbon\Carbon::parse($fc->date)->format('d/m/Y') }}</td>
            </tr>
            @empty
            <tr>
                <td colspan="5" class="text-center">Tidak ada catatan pemakaian pakan pada periode ini</td>
            </tr>
            @endforelse
        </tbody>
    </table>

    <div class="section-header">3. Rekam Medis &amp; Vaksinasi Periode Ini</div>
    <table class="data-table">
        <thead>
            <tr>
                <th width="5%" class="text-center">No</th>
                <th width="14%">Tag Sapi</th>
                <th width="14%">Tipe</th>
                <th width="35%">Diagnosis / Tindakan</th>
                <th width="14%" class="text-center">Tanggal</th>
                <th width="18%">Petugas</th>
            </tr>
        </thead>

        <tbody>
            @forelse($healthRecords as $idx => $hr)
            <tr>
                <td class="text-center">{{ $idx + 1 }}</td>
                <td>{{ $hr->livestock->tag_number ?? '-' }}</td>
                <td>{{ ucfirst($hr->type) }}</td>
                <td>{{ $hr->treatment ?? $hr->diagnosis ?? '-' }}</td>
                <td class="text-center">{{ \Carbon\Carbon::parse($hr->date)->format('d/m/Y') }}</td>
                <td>{{ $hr->handler->name ?? '-' }}</td>
            </tr>
            @empty
            <tr>
                <td colspan="6" class="text-center">Tidak ada rekam medis pada periode ini</td>
            </tr>
            @endforelse
        </tbody>
    </table>

    <table class="signature-table">
        <tr>
            <td>
                Mengetahui,<br>
                <strong>Penanggung Jawab Medis / Dokter Hewan</strong>
                <div class="signature-space"></div>
                ( ___________________________ )
            </td>
            <td>
                Sumedang, {{ now()->format('d F Y') }}<br>
                <strong>Manajer Operasional Peternakan</strong>
                <div class="signature-space"></div>
                ( <strong>{{ $generatedBy }}</strong> )
            </td>
        </tr>
    </table>

</body>
</html>
